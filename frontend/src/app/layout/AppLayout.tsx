import { useEffect, useMemo, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { Bell, Megaphone, ShoppingCart, LayoutDashboard, Search, Users, LogOut } from "lucide-react";

import { ChatbotFloatingButton } from "../components/ChatbotFloatingButton";
import { fetchWishlist } from "../api/wishlist";

export function AppLayout() {
	const navigate = useNavigate();
	const location = useLocation();

	const isHome = location.pathname === "/";
	const isAuthed = !!localStorage.getItem("accessToken");

	const [wishlistCount, setWishlistCount] = useState<number>(0);

	useEffect(() => {
		let mounted = true;

		(async () => {
			if (!isAuthed) {
				setWishlistCount(0);
				return;
			}
			try {
				const list = await fetchWishlist();
				if (!mounted) return;
				setWishlistCount(Array.isArray(list) ? list.length : 0);
			} catch {
				// 백엔드 미연동/에러여도 UI는 유지
				if (!mounted) return;
				setWishlistCount(0);
			}
		})();

		return () => {
			mounted = false;
		};
	}, [isAuthed]);

	const onLogout = () => {
		localStorage.removeItem("accessToken");
		localStorage.removeItem("refreshToken");
		localStorage.removeItem("userId");
		localStorage.removeItem("name");
		navigate("/", { replace: true });
	};

	return (
		<div className="min-h-screen flex flex-col bg-white">
			<header className="border-b bg-white">
				<div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
					{/* Logo */}
					<button
						type="button"
						onClick={() => navigate("/")}
						className="flex items-center gap-3 text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400 rounded-md"
						aria-label="홈으로 이동"
					>
						<div className="w-9 h-9 bg-blue-600 rounded-xl" />
						<div>
							<div className="font-bold leading-5">입찰인사이트</div>
							<div className="text-xs text-gray-500">Smart Procurement Platform</div>
						</div>
					</button>

					{/* 중앙 네비게이션 (홈에서는 숨김) */}
					{!isHome && (
						<nav className="flex items-center gap-2" aria-label="주요 메뉴">
							<TopNavLink to="/dashboard" icon={<LayoutDashboard className="h-4 w-4" />} label="대시보드" />
							<TopNavLink to="/bids" icon={<Search className="h-4 w-4" />} label="공고 찾기" />
							<TopNavLink
								to="/cart"
								icon={<ShoppingCart className="h-4 w-4" />}
								label="장바구니"
								badge={wishlistCount}
							/>
							<TopNavLink to="/community" icon={<Users className="h-4 w-4" />} label="커뮤니티" />
						</nav>
					)}

					{/* 우측 버튼 */}
					<div className="flex items-center gap-2">
						<RightActionButton
							label="공지사항"
							icon={<Megaphone className="h-4 w-4" />}
							onClick={() => navigate("/notice")}
						/>
						<RightActionButton
							label="알림"
							icon={<Bell className="h-4 w-4" />}
							onClick={() => navigate("/notifications")}
						/>

						{isAuthed && !isHome && (
							<RightActionButton
								label="로그아웃"
								icon={<LogOut className="h-4 w-4" />}
								onClick={onLogout}
							/>
						)}
					</div>
				</div>
			</header>

			<main className="flex-1">
				<Outlet />
			</main>

			<footer className="border-t py-6 text-sm text-gray-500 text-center bg-white">
				<div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
					<div>© 2026 입찰인사이트. All rights reserved.</div>
					<div className="flex items-center gap-6">
						<button className="hover:text-gray-700">이용약관</button>
						<button className="hover:text-gray-700">개인정보처리방침</button>
						<button className="hover:text-gray-700">고객지원</button>
					</div>
				</div>
			</footer>

			{/* 모든 페이지에서 챗봇 유지 */}
			<ChatbotFloatingButton />
		</div>
	);
}

function TopNavLink({
	to,
	label,
	icon,
	badge,
}: {
	to: string;
	label: string;
	icon: React.ReactNode;
	badge?: number;
}) {
	return (
		<NavLink
			to={to}
			className={({ isActive }) =>
				[
					"relative inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm transition",
					"focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400",
					isActive ? "bg-blue-50 text-blue-700" : "text-gray-700 hover:bg-gray-50",
				].join(" ")
			}
		>
			{icon}
			<span className="font-medium">{label}</span>

			{typeof badge === "number" && badge > 0 && (
				<span className="ml-1 inline-flex min-w-5 h-5 items-center justify-center rounded-full bg-black px-1.5 text-[11px] text-white">
					{badge}
				</span>
			)}
		</NavLink>
	);
}

function RightActionButton({
	label,
	icon,
	onClick,
}: {
	label: string;
	icon: React.ReactNode;
	onClick: () => void;
}) {
	return (
		<button
			type="button"
			onClick={onClick}
			className="inline-flex items-center gap-2 rounded-xl border px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400"
		>
			{icon}
			<span className="font-medium">{label}</span>
		</button>
	);
}
