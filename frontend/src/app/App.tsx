import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useState } from "react";

import { AppLayout } from "./layout/AppLayout";
import { PageContainer } from "./layout/PageContainer";

import { HomePage } from "./components/HomePage";
import { LoginPage } from "./components/LoginPage";
import { SignupPage } from "./components/SignupPage";
import { FindAccountPage } from "./components/FindAccount";
import { ResetPasswordPage } from "./components/ResetPasswordPage";

import { Dashboard } from "./components/Dashboard";
import { BidDiscovery } from "./components/BidDiscovery";
import { CartPage } from "./components/CartPage";
import { CommunityPage } from "./components/CommunityPage";
import { NoticePage } from "./components/NoticePage";
import { NotificationsPage } from "./components/NotificationsPage";
import { ProfilePage } from "./components/ProfilePage";

import { ProtectedRoute } from "./routes/ProtectedRoute";

import { Toast } from "./components/ui/Toast";
import { useToast } from "./components/ui/useToast";

function SignupRoute() {
  const navigate = useNavigate();
  return (
    <SignupPage
      onSignup={() => navigate("/login")}
      onNavigateToLogin={() => navigate("/login")}
    />
  );
}

function FindAccountRoute() {
  const navigate = useNavigate();
  return (
    <FindAccountPage
      onFindAccount={async () => {}}
      onNavigateToLogin={() => navigate("/login")}
    />
  );
}

function ResetPasswordRoute() {
  const navigate = useNavigate();
  return <ResetPasswordPage onNavigateToLogin={() => navigate("/login")} />;
}

export default function App() {
  const [globalLoading, setGlobalLoading] = useState(false);
  const { toast, showToast } = useToast();

  return (
    <BrowserRouter>
      {/* 전역 로딩/토스트 */}
      {globalLoading && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-[9999]">
          <div className="bg-white px-6 py-3 rounded-lg shadow">
            처리 중...
          </div>
        </div>
      )}
      {toast && <Toast message={toast.message} type={toast.type} />}

      <Routes>
        {/* ✅ 레이아웃 적용 구간 */}
        <Route element={<AppLayout />}>
          {/* 홈 */}
          <Route path="/" element={<HomePage />} />

          {/* 대시보드(보호) */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <Dashboard />
                </PageContainer>
              </ProtectedRoute>
            }
          />

          {/* 공고찾기(공개 조회 가능하게 두고 싶으면 ProtectedRoute 제거) */}
          <Route
            path="/bids"
            element={
              <PageContainer>
                <BidDiscovery setGlobalLoading={setGlobalLoading} showToast={showToast} />
              </PageContainer>
            }
          />

          {/* 장바구니(보호) */}
          <Route
            path="/cart"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <CartPage setGlobalLoading={setGlobalLoading} showToast={showToast} />
                </PageContainer>
              </ProtectedRoute>
            }
          />

          {/* 커뮤니티(보호) */}
          <Route
            path="/community"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <CommunityPage />
                </PageContainer>
              </ProtectedRoute>
            }
          />

          {/* 공지/알림/마이페이지(보호) */}
          <Route
            path="/notice"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <NoticePage />
                </PageContainer>
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <NotificationsPage />
                </PageContainer>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <ProfilePage />
                </PageContainer>
              </ProtectedRoute>
            }
          />
        </Route>

        {/* ✅ 인증 페이지는 단독 화면(원래 UI 유지) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupRoute />} />
        <Route path="/find-account" element={<FindAccountRoute />} />
        <Route path="/reset-password" element={<ResetPasswordRoute />} />
      </Routes>
    </BrowserRouter>
  );
}
