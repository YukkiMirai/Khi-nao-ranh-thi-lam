import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Header  from './components/Header'
import Footer  from './components/Footer'

export default function AdminLayout() {
  useEffect(() => {
    // Re-init Kaiadmin scrollbar plugin after React renders the sidebar
    const jq = (window as any).$
    if (jq) {
      try { jq('.scrollbar-inner').scrollbar() } catch { /* noop */ }
    }
    // Remove mobile sidebar overlay when navigating
    return () => {
      document.body.classList.remove('sidebar-show', 'topbar-open')
    }
  }, [])

  return (
    <div className="wrapper">
      <Sidebar />

      <div className="main-panel">
        <Header />

        <div className="container">
          <div className="page-inner">
            <Outlet />
          </div>
        </div>

        <Footer />
      </div>
    </div>
  )
}
