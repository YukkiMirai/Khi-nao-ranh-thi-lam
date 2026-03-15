import { Link, useLocation } from 'react-router-dom'
import type { Menu } from '../../../types'
import { useAppSelector } from '../../../hooks/common/useStore'

// ── Icon map – extend as you add menu types ────────────────────────────────
const ICON_MAP: Record<string, string> = {
  dashboard : 'fas fa-home',
  user      : 'fas fa-user',
  users     : 'fas fa-users',
  role      : 'fas fa-shield-alt',
  menu      : 'fas fa-bars',
  report    : 'far fa-chart-bar',
  setting   : 'fas fa-cog',
  default   : 'fas fa-circle',
}

function resolveIcon(menu: Menu): string {
  const key = (menu.menuId + menu.menuName).toLowerCase()
  for (const [k, cls] of Object.entries(ICON_MAP)) {
    if (key.includes(k)) return cls
  }
  return ICON_MAP.default
}

// ── Leaf menu item ─────────────────────────────────────────────────────────
function LeafItem({ menu }: { menu: Menu }) {
  const { pathname } = useLocation()
  const active = pathname.startsWith(menu.linkUri || '__none__')

  return (
    <li className={`nav-item${active ? ' active' : ''}`}>
      <Link to={menu.linkUri || '#'}>
        <i className={resolveIcon(menu)} />
        <p>{menu.menuNameVi || menu.menuName}</p>
      </Link>
    </li>
  )
}

// ── Collapsible parent item ────────────────────────────────────────────────
function ParentItem({ menu }: { menu: Menu }) {
  const { pathname } = useLocation()
  const collapseId = `nav-collapse-${menu.menuId}`
  const isActive = menu.children?.some(
    (c) => c.linkUri && pathname.startsWith(c.linkUri)
  )

  return (
    <li className={`nav-item${isActive ? ' active' : ''}`}>
      <a
        data-bs-toggle="collapse"
        href={`#${collapseId}`}
        className={isActive ? '' : 'collapsed'}
        aria-expanded={isActive ? 'true' : 'false'}
      >
        <i className={resolveIcon(menu)} />
        <p>{menu.menuNameVi || menu.menuName}</p>
        <span className="caret" />
      </a>
      <div
        className={`collapse${isActive ? ' show' : ''}`}
        id={collapseId}
      >
        <ul className="nav nav-collapse">
          {menu.children!.map((child) => (
            <SubItem key={child.menuId} menu={child} />
          ))}
        </ul>
      </div>
    </li>
  )
}

// ── Sub-menu item (inside a parent) ───────────────────────────────────────
function SubItem({ menu }: { menu: Menu }) {
  const { pathname } = useLocation()

  if (menu.children?.length) {
    // 3rd level – render as another collapsible
    return <ParentItem menu={menu} />
  }

  const active = menu.linkUri ? pathname.startsWith(menu.linkUri) : false

  return (
    <li className={active ? 'active' : ''}>
      <Link to={menu.linkUri || '#'}>
        <span className="sub-item">{menu.menuNameVi || menu.menuName}</span>
      </Link>
    </li>
  )
}

// ── Main sidebar ───────────────────────────────────────────────────────────
export default function Sidebar() {
  const menus = useAppSelector((s) => s.auth.profile?.menus ?? [])

  const handleToggleSidebar = () => document.body.classList.toggle('sidebar-mini')
  const handleSidenavToggler = () => document.body.classList.toggle('sidebar-show')

  return (
    <div className="sidebar" data-background-color="dark">
      {/* ── Logo header ── */}
      <div className="sidebar-logo">
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
              onClick={handleToggleSidebar}
              type="button"
            >
              <i className="gg-menu-right" />
            </button>
            <button
              className="btn btn-toggle sidenav-toggler"
              onClick={handleSidenavToggler}
              type="button"
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

      {/* ── Nav items ── */}
      <div className="sidebar-wrapper scrollbar scrollbar-inner">
        <div className="sidebar-content">
          <ul className="nav nav-secondary">
            {/* Static Dashboard link always present */}
            <li className="nav-item">
              <Link to="/dashboard">
                <i className="fas fa-home" />
                <p>Dashboard</p>
              </Link>
            </li>

            {/* Dynamic menus from API */}
            {menus.map((menu) =>
              menu.children?.length ? (
                <ParentItem key={menu.menuId} menu={menu} />
              ) : (
                <LeafItem key={menu.menuId} menu={menu} />
              )
            )}
          </ul>
        </div>
      </div>
    </div>
  )
}
