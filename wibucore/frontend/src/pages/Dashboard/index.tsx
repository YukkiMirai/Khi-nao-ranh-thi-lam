import { useAuth } from '../../hooks/common/useAuth'

export default function DashboardPage() {
  const { profile } = useAuth()
  const user = profile?.userInfo

  return (
    <>
      {/* Page header */}
      <div className="page-header">
        <h4 className="fw-bold mb-3">Dashboard</h4>
        <ul className="breadcrumbs  mb-3">
          <li className="nav-home">
            <a href="#"><i className="icon-home" /></a>
          </li>
          <li className="separator"><i className="icon-arrow-right" /></li>
          <li className="nav-item"><a href="#">Dashboard</a></li>
        </ul>
      </div>

      {/* Welcome card */}
      <div className="row">
        <div className="col-12">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">
                Xin chào,{' '}
                <strong>{user?.fullName || user?.userId || 'User'}</strong> 👋
              </h5>
              <p className="card-text text-muted">
                Chào mừng bạn đến với hệ thống quản lý phòng khám.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Stats row */}
      <div className="row">
        <div className="col-sm-6 col-md-3">
          <div className="card card-stats card-round">
            <div className="card-body">
              <div className="row align-items-center">
                <div className="col-icon">
                  <div className="icon-big text-center icon-primary bubble-shadow-small">
                    <i className="fas fa-users" />
                  </div>
                </div>
                <div className="col col-stats ms-3 ms-sm-0">
                  <div className="numbers">
                    <p className="card-category">Bệnh nhân</p>
                    <h4 className="card-title">—</h4>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-3">
          <div className="card card-stats card-round">
            <div className="card-body">
              <div className="row align-items-center">
                <div className="col-icon">
                  <div className="icon-big text-center icon-success bubble-shadow-small">
                    <i className="fas fa-calendar-check" />
                  </div>
                </div>
                <div className="col col-stats ms-3 ms-sm-0">
                  <div className="numbers">
                    <p className="card-category">Lịch hẹn hôm nay</p>
                    <h4 className="card-title">—</h4>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-3">
          <div className="card card-stats card-round">
            <div className="card-body">
              <div className="row align-items-center">
                <div className="col-icon">
                  <div className="icon-big text-center icon-warning bubble-shadow-small">
                    <i className="fas fa-stethoscope" />
                  </div>
                </div>
                <div className="col col-stats ms-3 ms-sm-0">
                  <div className="numbers">
                    <p className="card-category">Đang điều trị</p>
                    <h4 className="card-title">—</h4>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-md-3">
          <div className="card card-stats card-round">
            <div className="card-body">
              <div className="row align-items-center">
                <div className="col-icon">
                  <div className="icon-big text-center icon-danger bubble-shadow-small">
                    <i className="fas fa-file-invoice-dollar" />
                  </div>
                </div>
                <div className="col col-stats ms-3 ms-sm-0">
                  <div className="numbers">
                    <p className="card-category">Doanh thu</p>
                    <h4 className="card-title">—</h4>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Roles card */}
      {profile?.roles && profile.roles.length > 0 && (
        <div className="row mt-2">
          <div className="col-12">
            <div className="card">
              <div className="card-header">
                <div className="card-title">Vai trò của bạn</div>
              </div>
              <div className="card-body">
                <div className="d-flex flex-wrap gap-2">
                  {profile.roles.map((r) => (
                    <span key={r.roleId} className="badge badge-primary">
                      {r.name}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
