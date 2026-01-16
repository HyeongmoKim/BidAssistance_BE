import { useNavigate } from "react-router-dom";

export function Home() {
  const navigate = useNavigate();

  return (
    <div className="space-y-4">
      {/* 상단 Quick Actions */}
      <div className="grid grid-cols-4 gap-3">
        <ActionCard title="대시보드" desc="현황/통계 한눈에" onClick={() => navigate("/dashboard")} />
        <ActionCard title="공고 찾기" desc="조건 필터/검색" onClick={() => navigate("/bids")} />
        <ActionCard title="장바구니" desc="wishlist 관리" onClick={() => navigate("/cart")} />
        <ActionCard title="커뮤니티" desc="정보 공유/질문" onClick={() => navigate("/community")} />
      </div>

      <div className="grid grid-cols-12 gap-4">
        {/* AI 검색 패널 */}
        <section className="col-span-8 rounded-xl border bg-white p-4">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 className="text-base font-semibold text-gray-900">✨ AI 기반 공고 검색</h2>
              <p className="mt-1 text-sm text-gray-600">
                자연어로 입력하면 조건을 해석해 공고 탐색/분석 흐름으로 연결합니다.
              </p>
            </div>
            <button
              className="text-sm font-medium text-blue-700 hover:text-blue-800"
              onClick={() => navigate("/bids")}
            >
              공고 리스트 →
            </button>
          </div>

          <div className="mt-3 flex gap-2">
            <input
              className="flex-1 h-10 rounded-lg px-3 bg-gray-50 border border-transparent focus:border-blue-300 focus:outline-none"
              placeholder='예: "서울/경기 10억~50억 시설공사, 마감 임박 우선"'
            />
            <button className="h-10 px-4 rounded-lg bg-black text-white text-sm font-medium">
              AI로 검색
            </button>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3">
            <MiniPanel title="추천 키워드" items={["마감 임박", "서울/경기", "10~50억", "시설공사"]} />
            <MiniPanel title="최근 검색" items={["관급 리모델링", "전기공사 3억 이하", "인천 마감 7일"]} />
          </div>
        </section>

        {/* 로그인/온보딩 패널 */}
        <aside className="col-span-4 rounded-xl border bg-white p-4">
          <h3 className="text-base font-semibold text-gray-900">로그인</h3>
          <p className="mt-1 text-sm text-gray-600">
            로그인하면 장바구니/알림/AI 기능을 이용할 수 있습니다.
          </p>

          <div className="mt-3 space-y-2">
            <button
              className="w-full h-10 rounded-lg bg-black text-white text-sm font-medium"
              onClick={() => navigate("/login")}
            >
              로그인 페이지로 이동
            </button>
            <button
              className="w-full h-10 rounded-lg border text-sm font-medium hover:bg-gray-50"
              onClick={() => navigate("/signup")}
            >
              회원가입
            </button>
          </div>

          <div className="mt-4 rounded-lg bg-slate-50 border p-3">
            <div className="text-xs text-gray-500">TIP</div>
            <div className="mt-1 text-sm text-gray-800">
              관심 공고를 담아두면 마감/정정 알림을 자동으로 받아볼 수 있어요.
            </div>
          </div>
        </aside>

        {/* 아래 리스트로 “휑함” 제거 */}
        <section className="col-span-8 rounded-xl border bg-white p-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-semibold text-gray-900">추천 공고 (샘플)</h3>
            <button className="text-sm font-medium text-blue-700 hover:text-blue-800" onClick={() => navigate("/bids")}>
              더보기 →
            </button>
          </div>

          <div className="mt-3 divide-y">
            <ListRow title="광주시 광산구 문화체육시설 신축" meta="45억 · 2026-01-18" />
            <ListRow title="대전시 유성구 복지센터 리모델링" meta="8억 · 2026-01-20" />
            <ListRow title="인천 ○○초 교사동 증축" meta="12억 · 2026-01-22" />
          </div>
        </section>

        <section className="col-span-4 rounded-xl border bg-white p-4">
          <h3 className="text-base font-semibold text-gray-900">알림 요약 (샘플)</h3>
          <div className="mt-3 space-y-2">
            <AlertItem title="마감 임박 1건" desc="24시간 이내 공고가 있어요" />
            <AlertItem title="정정 공고 1건" desc="관심 공고에 변경사항" />
            <AlertItem title="재공고 1건" desc="조건 재확인 필요" />
          </div>
        </section>
      </div>
    </div>
  );
}

function ActionCard({ title, desc, onClick }: { title: string; desc: string; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-3 rounded-xl border bg-white p-3 hover:bg-gray-50 transition text-left"
    >
      <div className="w-9 h-9 bg-slate-100 rounded-lg" />
      <div>
        <div className="font-semibold text-sm text-gray-900">{title}</div>
        <div className="text-xs text-gray-500">{desc}</div>
      </div>
    </button>
  );
}

function MiniPanel({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="rounded-lg border bg-slate-50 p-3">
      <div className="text-xs font-semibold text-gray-700">{title}</div>
      <div className="mt-2 flex flex-wrap gap-2">
        {items.map((it) => (
          <span key={it} className="text-xs px-2 py-1 rounded-full bg-white border text-gray-700">
            {it}
          </span>
        ))}
      </div>
    </div>
  );
}

function ListRow({ title, meta }: { title: string; meta: string }) {
  return (
    <div className="py-3 flex items-center justify-between gap-4">
      <div className="font-medium text-sm text-gray-900">{title}</div>
      <div className="text-xs text-gray-500 whitespace-nowrap">{meta}</div>
    </div>
  );
}

function AlertItem({ title, desc }: { title: string; desc: string }) {
  return (
    <div className="rounded-lg border bg-slate-50 p-3">
      <div className="text-sm font-semibold text-gray-900">{title}</div>
      <div className="mt-1 text-xs text-gray-600">{desc}</div>
    </div>
  );
}
