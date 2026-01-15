import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "./ui/card";
import { Building2 } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";

const SECURITY_QUESTIONS = [
    "가장 기억에 남는 선생님 성함은?",
    "첫 반려동물 이름은?",
    "출생한 도시는?",
    "가장 좋아하는 음식은?",
] as const;

// legacy(키 문자열 저장) → 숫자 인덱스로 변환(과거 데이터 호환용)
const LEGACY_KEY_TO_INDEX: Record<string, number> = {
    favorite_teacher: 0,
    first_pet: 1,
    birth_city: 2,
    favorite_food: 3,
};

type LocalUser = {
    email: string;
    name: string;
    birthDate: string;
    recoveryQA: {
        questionIndex?: number;
        question?: string; // legacy
        answer: string;
    };
};

const LS_USERS_KEY = "bidassistance_users_v1";

function readUsers(): LocalUser[] {
    try {
        const raw = localStorage.getItem(LS_USERS_KEY);
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? (parsed as LocalUser[]) : [];
    } catch {
        return [];
    }
}

function normalize(s: string) {
    return s.trim().toLowerCase();
}

function resolveQuestionIndex(recoveryQA: {
    questionIndex?: number;
    question?: string;
}): number | null {
    const idx = recoveryQA?.questionIndex;

    if (
        typeof idx === "number" &&
        Number.isInteger(idx) &&
        idx >= 0 &&
        idx < SECURITY_QUESTIONS.length
    ) {
        return idx;
    }

    const legacy = (recoveryQA?.question ?? "").trim();
    if (legacy && Object.prototype.hasOwnProperty.call(LEGACY_KEY_TO_INDEX, legacy)) {
        return LEGACY_KEY_TO_INDEX[legacy as keyof typeof LEGACY_KEY_TO_INDEX];
    }

    return null;
}

interface FindAccountPageProps {
    onFindAccount: (payload: {
        name: string;
        birthDate: string; // YYYY-MM-DD
        questionIndex: number;
        answer: string;
    }) => void | Promise<void>;
    onNavigateToLogin: () => void;
}

export function FindAccountPage({ onFindAccount, onNavigateToLogin }: FindAccountPageProps) {
    const [formData, setFormData] = useState({
        name: "",
        birthDate: "",
        answer: "",
    });

    const [step, setStep] = useState<"identify" | "answer" | "result">("identify");
    const [questionIndex, setQuestionIndex] = useState<number | null>(null);
    const [identifiedEmail, setIdentifiedEmail] = useState<string>(""); // 결과로 보여줄 계정(이메일)
    const [targetEmail, setTargetEmail] = useState<string>("");

    // const handleSubmit = (e: React.FormEvent) => {
    //     e.preventDefault();
    //     if (!formData.name || !formData.birthDate || !formData.questionIndex || !formData.answer) return;
    //
    //     onFindAccount({
    //         name: formData.name,
    //         birthDate: formData.birthDate,
    //         questionIndex: formData.questionIndex,
    //         answer: formData.answer,
    //     });
    // };
    const handleIdentify = (e: React.FormEvent) => {
        e.preventDefault();

        const name = formData.name.trim();
        const birthDate = formData.birthDate;

        if (!name || !birthDate) return;

        const users = readUsers();

        // 이름+생년월일로 후보 찾기 (운영이면 추가 식별자 더 받는 걸 추천)
        const target = users.find(
            (u) => normalize(u.name) === normalize(name) && u.birthDate === birthDate
        );

        if (!target) {
            // 여기서는 간단히 alert 대신 상태 메시지 UI로 바꾸면 더 좋음
            alert("일치하는 계정을 찾을 수 없어요.");
            return;
        }

        const qIndex = resolveQuestionIndex(target.recoveryQA);
        if (qIndex === null) {
            alert("계정에 복구 질문이 설정되어 있지 않아요.");
            return;
        }

        setTargetEmail(target.email);
        setQuestionIndex(qIndex);
        setStep("answer");
    };

    const handleVerifyAnswer = (e: React.FormEvent) => {
        e.preventDefault();

        if (questionIndex === null) return;

        const answer = formData.answer.trim();
        if (!answer) return;

        const users = readUsers();
        const target = users.find((u) => normalize(u.email) === normalize(targetEmail));

        if (!target) {
            alert("계정을 찾을 수 없어요. 다시 시도해 주세요.");
            setStep("identify");
            return;
        }

        // 질문 인덱스가 바뀌었는지(데이터 꼬임 방지)
        const storedQIndex = resolveQuestionIndex(target.recoveryQA);
        if (storedQIndex === null || storedQIndex !== questionIndex) {
            alert("질문 정보가 일치하지 않아요. 다시 시도해 주세요.");
            setStep("identify");
            return;
        }

        // 답변 비교 (운영이면 해시 비교해야 함)
        if (normalize(target.recoveryQA.answer) !== normalize(answer)) {
            alert("답변이 일치하지 않아요.");
            return;
        }

        setIdentifiedEmail(target.email);
        setStep("result");
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
            <Card className="w-full max-w-md">
                <CardHeader className="space-y-1">
                    <div className="flex items-center justify-center mb-4">
                        <div className="bg-blue-600 p-3 rounded-lg">
                            <Building2 className="w-8 h-8 text-white" />
                        </div>
                    </div>
                    <CardTitle className="text-2xl text-center">계정 찾기</CardTitle>
                    <CardDescription className="text-center">
                        가입 시 등록한 정보로 이메일(계정)을 확인합니다
                    </CardDescription>
                </CardHeader>

                <form onSubmit={step === "identify" ? handleIdentify : step === "answer" ? handleVerifyAnswer : (e) => e.preventDefault()}>
                    <CardContent className="space-y-4">
                        {/* 1) identify 단계: 이름 + 생년월일 */}
                        {step === "identify" && (
                            <>
                                <div className="space-y-2">
                                    <Label htmlFor="name">이름</Label>
                                    <Input
                                        id="name"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        required
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="birthDate">생년월일</Label>
                                    <Input
                                        id="birthDate"
                                        type="date"
                                        value={formData.birthDate}
                                        onChange={(e) => setFormData({ ...formData, birthDate: e.target.value })}
                                        required
                                    />
                                </div>
                            </>
                        )}

                        {/* 2) answer 단계: 질문 출력 + 답변 입력 + 다시 입력 */}
                        {step === "answer" && (
                            <>
                                <div className="space-y-2">
                                    <Label>가입 시 설정한 질문</Label>
                                    <div className="rounded-md border bg-white px-3 py-2 text-sm">
                                        {questionIndex !== null ? SECURITY_QUESTIONS[questionIndex] : ""}
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="answer">답변</Label>
                                    <Input
                                        id="answer"
                                        value={formData.answer}
                                        onChange={(e) => setFormData({ ...formData, answer: e.target.value })}
                                        required
                                    />
                                </div>

                                <Button
                                    type="button"
                                    variant="outline"
                                    className="w-full"
                                    onClick={() => {
                                        setStep("identify");
                                        setQuestionIndex(null);
                                        setTargetEmail("");
                                        setIdentifiedEmail("");
                                        setFormData({ ...formData, answer: "" });
                                    }}
                                >
                                    다시 입력하기
                                </Button>
                            </>
                        )}

                        {/* 3) result 단계: 계정(이메일) 공개 */}
                        {step === "result" && (
                            <>
                                <div className="rounded-md bg-green-50 text-green-700 px-3 py-2 text-sm">
                                    계정을 찾았어요!
                                </div>

                                <div className="text-sm">
                                    당신의 계정(이메일): <span className="font-medium">{identifiedEmail}</span>
                                </div>

                                <Button type="button" className="w-full" onClick={onNavigateToLogin}>
                                    로그인하러 가기
                                </Button>

                                <Button
                                    type="button"
                                    variant="outline"
                                    className="w-full"
                                    onClick={() => {
                                        setStep("identify");
                                        setQuestionIndex(null);
                                        setTargetEmail("");
                                        setIdentifiedEmail("");
                                        setFormData({ name: "", birthDate: "", answer: "" });
                                    }}
                                >
                                    다시 찾기
                                </Button>
                            </>
                        )}
                    </CardContent>


                    <CardFooter className="flex flex-col space-y-4">
                        {step !== "result" && (
                            <Button type="submit" className="w-full">
                                {step === "identify" ? "다음" : "계정 확인"}
                            </Button>
                        )}


                        <div className="text-sm text-center text-gray-600">
                            로그인 화면으로 돌아가기{" "}
                            <button
                                type="button"
                                onClick={onNavigateToLogin}
                                className="text-blue-600 hover:underline"
                            >
                                로그인
                            </button>
                        </div>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
}
