export function Footer() {
  return (
    <footer className="border-t border-gray-200 bg-white px-6 py-3 text-center text-xs text-gray-500">
      EmployeeCrudEventDriven &middot; v1.0.0 &middot;
      {' '}
      <a
        href="/service/swagger-ui.html"
        target="_blank"
        rel="noopener noreferrer"
        className="underline hover:text-gray-700"
      >
        Employee API
      </a>
      {' '}&middot;{' '}
      <a
        href="/users/swagger-ui.html"
        target="_blank"
        rel="noopener noreferrer"
        className="underline hover:text-gray-700"
      >
        Users API
      </a>
      {' '}&middot; &copy;{new Date().getFullYear()}
    </footer>
  )
}
