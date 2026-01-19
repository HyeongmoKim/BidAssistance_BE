import { useEffect, useState } from "react";
import { fetchWishlist, updateWishlistStage, deleteWishlist } from "../api/wishlist";
import {
	BID_STAGE_OPTIONS,
	isBidStage,
	type BidStage,
	type WishlistItem,
} from "../types/wishlist";

export function CartPage({
	setGlobalLoading,
	showToast,
}: {
	setGlobalLoading: (v: boolean) => void;
	showToast: (msg: string, type: "success" | "error") => void;
}) {
	const [wishlist, setWishlist] = useState<WishlistItem[]>([]);

	useEffect(() => {
		fetchWishlist().then(setWishlist);
	}, []);

	const changeStage = async (id: number, stage: BidStage) => {
		try {
			setGlobalLoading(true);
			await updateWishlistStage(id, stage);
			setWishlist((prev) =>
				prev.map((w) => (w.wishlistId === id ? { ...w, stage } : w)),
			);
			showToast("상태 변경 완료", "success");
		} catch {
			showToast("상태 변경 실패", "error");
		} finally {
			setGlobalLoading(false);
		}
	};

	const remove = async (id: number) => {
		try {
			setGlobalLoading(true);
			await deleteWishlist(id);
			setWishlist((prev) => prev.filter((w) => w.wishlistId !== id));
			showToast("삭제되었습니다", "success");
		} finally {
			setGlobalLoading(false);
		}
	};

	return (
		<div className="space-y-4">
			<h2 className="text-2xl font-bold">장바구니</h2>

			{wishlist.map((w) => (
				<div key={w.wishlistId} className="border p-3 flex justify-between">
					<div>
						<div className="font-semibold">{w.title}</div>
						<div className="text-sm text-muted-foreground">
							{w.budget} · {w.deadline}
						</div>
					</div>

					<div className="flex gap-2">
						<select
							value={w.stage}
							onChange={(e) => {
								const next = e.target.value;
								if (!isBidStage(next)) return;
								changeStage(w.wishlistId, next);
							}}
						>
							{BID_STAGE_OPTIONS.map((opt) => (
								<option key={opt.value} value={opt.value}>
									{opt.label}
								</option>
							))}
						</select>

						<button onClick={() => remove(w.wishlistId)}>삭제</button>
					</div>
				</div>
			))}
		</div>
	);
}
