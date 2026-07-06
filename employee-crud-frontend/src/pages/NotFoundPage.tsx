import { useNavigate } from 'react-router-dom'
import { Home } from 'lucide-react'

export function NotFoundPage() {
  const navigate = useNavigate()

  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <p className="text-6xl font-bold text-gray-300">404</p>
      <h1 className="mt-4 text-xl font-semibold text-gray-900">Page not found</h1>
      <p className="mt-1 text-sm text-gray-500">The page you are looking for does not exist.</p>
      <button
        onClick={() => navigate('/')}
        className="mt-6 flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
      >
        <Home size={16} />
        Go Home
      </button>
    </div>
  )
}
