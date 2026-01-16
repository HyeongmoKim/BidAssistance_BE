import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AppLayout } from "./layout/AppLayout";
import { MainPage } from "./components/pages/MainPage";
import { LoginPage } from "./components/pages/LoginPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<MainPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
