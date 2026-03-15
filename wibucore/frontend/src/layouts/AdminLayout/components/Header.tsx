import { Link } from 'react-router-dom'
import { useAuth } from '../../../hooks/common/useAuth'

export default function Header() {
  const { profile, logout } = useAuth()
  const user = profile?.userInfo

  const displayName = user?.fullName || user?.userId || 'User'
  const initial     = displayName.charAt(0).toUpperCase()

  const handleToggle     = () => document.body.classList.toggle('sidebar-mini')
  const handleSidenavToggle = () => document.body.classList.toggle('sidebar-show')

  return (
    <div className="main-header">
      {/* ── Mobile logo (mirrors sidebar logo-header for breakpoints) ── */}
      <div className="main-header-logo">
        <div className="logo-header" data-background-color="dark">
          <Link to="/dashboard" className="logo">
            <img
              src="/assets/img/kaiadmin/logo_light.svg"
              alt="Clinic App"
              className="navbar-brand"
              height="20"
            />
          </Link>
          <div className="nav-toggle">
            <button
              className="btn btn-toggle toggle-sidebar"
              type="button"
              onClick={handleToggle}
            >
              <i className="gg-menu-right" />
            </button>
            <button
              className="btn btn-toggle sidenav-toggler"
              type="button"
              onClick={handleSidenavToggle}
            >
              <i className="gg-menu-left" />
            </button>
          </div>
          <button
            className="topbar-toggler more"
            type="button"
            onClick={() => document.body.classList.toggle('topbar-open')}
          >
            <i className="gg-more-vertical-alt" />
          </button>
        </div>
      </div>

      {/* ── Main navbar ── */}
      <nav className="navbar navbar-header navbar-header-transparent navbar-expand-lg border-bottom">
        <div className="container-fluid">
          {/* Search box */}
          <nav className="navbar navbar-header-left navbar-expand-lg navbar-form nav-search p-0 d-none d-lg-flex">
            <div className="input-group">
              <div className="input-group-prepend">
                <button type="button" className="btn btn-search pe-1">
                  <i className="fa fa-search search-icon" />
                </button>
              </div>
              <input
                type="text"
                placeholder="Tìm kiếm..."
                className="form-control"
              />
            </div>
          </nav>

          {/* Right side: user dropdown */}
          <ul className="navbar-nav topbar-nav ms-md-auto align-items-center">
            <li className="nav-item topbar-user dropdown hidden-caret">
              <a
                className="dropdown-toggle profile-pic"
                data-bs-toggle="dropdown"
                href="#"
                aria-expanded="false"
                onClick={(e) => e.preventDefault()}
              >
                {/* Avatar: show photo placeholder with initial */}
                <div
                  className="avatar-sm d-flex align-items-center justify-content-center rounded-circle text-white fw-bold"
                  style={{ background: '#aa3bff', width: 35, height: 35, fontSize: 14 }}
                >
                  {initial}
                </div>
                <span className="profile-username ms-2">
                  <span className="op-7">Xin chào, </span>
                  <span className="fw-bold">{displayName}</span>
                </span>
              </a>

              <ul className="dropdown-menu dropdown-user animated fadeIn">
                <div className="dropdown-user-scroll scrollbar-outer">
                  <li>
                    <div className="user-box">
                      <div
                        className="avatar-lg d-flex align-items-center justify-content-center rounded text-white fw-bold"
                        style={{ background: '#aa3bff', width: 50, height: 50, fontSize: 20 }}
                      >
                        {initial}
                      </div>
                      <div className="u-text">
                        <h4>{displayName}</h4>
                        <p className="text-muted">{user?.email || ''}</p>
                      </div>
                    </div>
                  </li>
                  <li>
                    <div className="dropdown-divider" />
                    <Link className="dropdown-item" to="/profile">
                      Hồ sơ cá nhân
                    </Link>
                    <div className="dropdown-divider" />
                    <button
                      className="dropdown-item text-danger"
                      onClick={logout}
                    >
                      Đăng xuất
                    </button>
                  </li>
                </div>
              </ul>
            </li>
          </ul>
        </div>
      </nav>
    </div>
  )
}
