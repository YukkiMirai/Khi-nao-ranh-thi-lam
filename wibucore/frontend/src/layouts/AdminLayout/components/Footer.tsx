export default function Footer() {
  const year = new Date().getFullYear()

  return (
    <footer className="footer">
      <div className="container-fluid d-flex align-items-center justify-content-between py-2">
        <div className="copyright">
          © {year} <strong>Clinic App</strong>. All rights reserved.
        </div>
        <div className="d-flex align-items-center gap-2">
          <span className="text-muted" style={{ fontSize: 12 }}>
            Powered by Wibuneverdie Service
          </span>
        </div>
      </div>
    </footer>
  )
}
