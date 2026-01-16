import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useState } from "react";

import { AppLayout } from "./layout/AppLayout";
import { PageContainer } from "./layout/PageContainer";

import { ProtectedRoute } from "./routes/ProtectedRoute";

/* Pages/Components */
import { HomePage } from "./components/HomePage";
import { LoginPage } from "./components/LoginPage";
import { SignupPage } from "./components/SignupPage";
import { FindAccountPage } from "./components/FindAccount";
import { ResetPasswordPage } from "./components/ResetPasswordPage";

import { Dashboard } from "./components/Dashboard";
import { BidDiscovery } from "./components/BidDiscovery";
import { CartPage } from "./components/CartPage";
import { CommunityPage } from "./components/CommunityPage";

/* Toast */
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
      {globalLoading && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white px-6 py-3 rounded">처리 중...</div>
        </div>
      )}
      {toast && <Toast message={toast.message} type={toast.type} />}

      <Routes>
        <Route element={<AppLayout />}>
          {/* Public */}
          <Route path="/" element={<HomePage />} />

          {/* Protected (로그인 필요) */}
          <Route element={<ProtectedRoute />}>
            <Route
              path="/dashboard"
              element={
                <PageContainer>
                  <Dashboard />
                </PageContainer>
              }
            />
            <Route
              path="/bids"
              element={
                <PageContainer>
                  <BidDiscovery
                    setGlobalLoading={setGlobalLoading}
                    showToast={showToast}
                  />
                </PageContainer>
              }
            />
            <Route
              path="/cart"
              element={
                <PageContainer>
                  <CartPage
                    setGlobalLoading={setGlobalLoading}
                    showToast={showToast}
                  />
                </PageContainer>
              }
            />
            <Route
              path="/community"
              element={
                <PageContainer>
                  <CommunityPage />
                </PageContainer>
              }
            />
          </Route>
        </Route>

        {/* Auth pages (layout 미적용) */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupRoute />} />
        <Route path="/find-account" element={<FindAccountRoute />} />
        <Route path="/reset-password" element={<ResetPasswordRoute />} />
      </Routes>
    </BrowserRouter>
  );
}
