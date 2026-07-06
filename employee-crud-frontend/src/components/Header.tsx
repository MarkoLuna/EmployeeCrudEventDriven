import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { User, Settings, LogOut, ChevronDown } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'

export function Header() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <header className="flex h-14 items-center justify-end border-b border-gray-200 bg-white px-6">
      <div className="relative" ref={ref}>
        <button
          onClick={() => setOpen(!open)}
          className="flex items-center gap-2 rounded-md px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-100"
        >
          <User size={18} />
          <span>{user?.email ?? user?.username}</span>
          <ChevronDown size={14} />
        </button>

        {open && (
          <div className="absolute right-0 top-full z-40 mt-1 w-48 rounded-md border border-gray-200 bg-white py-1 shadow-lg">
            <button
              onClick={() => { setOpen(false); navigate('/profile') }}
              className="flex w-full items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
            >
              <User size={16} />
              My Profile
            </button>
            <button
              onClick={() => { setOpen(false); navigate('/profile') }}
              className="flex w-full items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
            >
              <Settings size={16} />
              Settings
            </button>
            <hr className="my-1 border-gray-200" />
            <button
              onClick={() => { setOpen(false); logout() }}
              className="flex w-full items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-gray-100"
            >
              <LogOut size={16} />
              Sign Out
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
