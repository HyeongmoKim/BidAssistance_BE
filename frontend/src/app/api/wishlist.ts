import { api } from "./client";
import type { BidStage, WishlistItem } from "../types/wishlist";

export function fetchWishlist() {
	return api<WishlistItem[]>("/wishlist");
}

export function updateWishlistStage(wishlistId: number, stage: BidStage) {
	return api(`/wishlist/${wishlistId}/stage`, {
		method: "PATCH",
		body: JSON.stringify({ stage }),
	});
}

export function deleteWishlist(wishlistId: number) {
	return api(`/wishlist/${wishlistId}`, {
		method: "DELETE",
	});
}
