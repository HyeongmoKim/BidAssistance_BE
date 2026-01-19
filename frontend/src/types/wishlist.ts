export const BID_STAGES = [
	"INTEREST",
	"REVIEW",
	"DECIDED",
	"DOC_PREP",
	"SUBMITTED",
	"WON",
	"LOST",
] as const;

export type BidStage = (typeof BID_STAGES)[number];

export function isBidStage(v: string): v is BidStage {
	return (BID_STAGES as readonly string[]).includes(v);
}

export const BID_STAGE_OPTIONS: Array<{ value: BidStage; label: string }> = [
	{ value: "INTEREST", label: "관심" },
	{ value: "REVIEW", label: "검토중" },
	{ value: "DECIDED", label: "참여결정" },
	{ value: "DOC_PREP", label: "서류준비" },
	{ value: "SUBMITTED", label: "제출완료" },
	{ value: "WON", label: "낙찰" },
	{ value: "LOST", label: "탈락" },
];

export interface WishlistItem {
	wishlistId: number;
	bidId: number;
	title: string;
	agency: string;
	budget: string;
	budgetValue: number;
	deadline: string;
	stage: BidStage;
}
