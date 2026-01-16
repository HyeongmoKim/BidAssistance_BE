import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { ChatbotFloatingButton } from "../components/ChatbotFloatingButton";

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const isHome = location.pathname === "/";

  return (
    <div className="min-h-screen flex flex-col bg-white">
      <header className="border-b bg-white sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-5 h-16 flex items-center justify-between">
          {/* Logo */}
          <button
            type="button"
            onClick={() => navigate("/")}
            className="flex items-center gap-3 text-left rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400"
          >
            <div className="w-9 h-9 bg-blue-600 rounded-xl" />
            <div>
              <div className="font-bold leading-5">입찰인사이트</div>
              <div className="text-xs text-gray-500">Smart Procurement Platform</div>
            </div>
          </button>

          {/* 중앙 네비게이션 (홈에서는 숨김) */}
          {!isHome && (
            <nav className="flex items-center gap-2">
              <NavButton label="대시보드" onClick={() => navigate("/dashboard")} />
              <NavButton label="공고 찾기" onClick={() => navigate("/bids")} />
              <NavButton label="장바구니" onClick={() => navigate("/cart")} />
              <NavButton label="커뮤니티" onClick={() => navigate("/community")} />
            </nav>
          )}

          {/* 우측 버튼 */}
          <div className="flex items-center gap-2">
            <RightButton label="공지사항" onClick={() => navigate("/notice")} />
            <RightButton label="알림" onClick={() => navigate("/notifications")} />
          </div>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t py-5 text-sm text-gray-500 bg-white">
        <div className="max-w-7xl mx-auto px-5 flex items-center justify-between">
          <div>© 2026 입찰인사이트. All rights reserved.</div>
          <div className="flex items-center gap-6">
            <button className="hover:text-gray-700">이용약관</button>
            <button className="hover:text-gray-700">개인정보처리방침</button>
            <button className="hover:text-gray-700">고객지원</button>
          </div>
        </div>
      </footer>

      <ChatbotFloatingButton />
    </div>
  );
}

function NavButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="px-4 py-2 rounded-full text-sm font-medium text-gray-700 hover:bg-gray-50 border border-transparent hover:border-gray-200 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400"
    >
      {label}
    </button>
  );
}

function RightButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className="px-3 py-2 rounded-xl border text-sm font-medium text-gray-700 hover:bg-gray-50 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-400"
    >
      {label}
    </button>
  );
}
