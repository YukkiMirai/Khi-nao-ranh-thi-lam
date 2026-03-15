import { Routes, Route, Navigate } from 'react-router-dom'
import { PrivateRoute }  from './PrivateRoute'
import AdminLayout       from '../layouts/AdminLayout'
import LoginPage         from '../pages/Login'
import DashboardPage     from '../pages/Dashboard'

/**
 * Khai báo tất cả routes của ứng dụng tại một nơi.
 *
 * Public  routes — truy cập tự do (Login).
 * Private routes — cần JWT hợp lệ → bọc trong AdminLayout.
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* ── Public ────────────────────────────────────────────── */}
      <Route path="/login" element={<LoginPage />} />

      {/* ── Private (yêu cầu xác thực + AdminLayout) ─────────── */}
      <Route element={<PrivateRoute />}>
        <Route element={<AdminLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          {/* Thêm các private routes tại đây */}
        </Route>
      </Route>

      {/* ── Fallback ──────────────────────────────────────────── */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
