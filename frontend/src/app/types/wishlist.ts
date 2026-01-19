import type { BidStage } from "./bid";

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
