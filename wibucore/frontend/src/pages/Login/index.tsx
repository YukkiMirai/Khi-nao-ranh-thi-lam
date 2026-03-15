import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/common/useAuth'
import './Login.css'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, loading, error, isAuthenticated, clearError } = useAuth()

  const [userId, setUserId]     = useState('')
  const [password, setPassword] = useState('')

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true })
    }
  }, [isAuthenticated, navigate])

  // Clear stale errors when inputs change
  useEffect(() => {
    if (error) clearError()
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, password])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    login({ userId, password })
  }

  return (
    <div className="login-root">
      <div className="login-card">
        {/* Logo / Branding */}
        <div className="login-brand">
          <span className="login-brand-dot" />
          <h1 className="login-title">Clinic App</h1>
        </div>
        <p className="login-subtitle">Đăng nhập vào hệ thống</p>

        <form onSubmit={handleSubmit} className="login-form" noValidate>
          {error && (
            <div className="login-alert" role="alert">
              {String(error)}
            </div>
          )}

          <div className="login-field">
            <label htmlFor="userId">Tên đăng nhập</label>
            <input
              id="userId"
              type="text"
              autoComplete="username"
              placeholder="Nhập tên đăng nhập"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <div className="login-field">
            <label htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              placeholder="Nhập mật khẩu"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? (
              <>
                <span className="login-spinner" aria-hidden="true" />
                Đang đăng nhập…
              </>
            ) : (
              'Đăng nhập'
            )}
          </button>
        </form>
      </div>
    </div>
  )
}
