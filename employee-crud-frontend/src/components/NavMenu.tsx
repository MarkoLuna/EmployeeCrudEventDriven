import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Users, Briefcase, UserCircle } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, roles: undefined },
  { to: '/employees', label: 'Employees', icon: Briefcase, roles: undefined },
  { to: '/users', label: 'Users', icon: Users, roles: ['manage-users', 'view-users', 'query-users'] },
  { to: '/profile', label: 'My Profile', icon: UserCircle, roles: undefined },
]

export function NavMenu() {
  const { hasAnyRole } = useAuth()

  const visible = navItems.filter(
    (item) => !item.roles || item.roles.length === 0 || hasAnyRole(item.roles),
  )

  return (
    <aside className="flex w-56 flex-col border-r border-gray-200 bg-white">
      <div className="flex h-14 items-center border-b border-gray-200 px-5">
        <span className="text-lg font-bold text-indigo-600">Employee CRUD</span>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-4">
        {visible.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-indigo-50 text-indigo-700'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              }`
            }
          >
            <item.icon size={18} />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-gray-200 px-4 py-3">
        <p className="text-xs text-gray-400">EmployeeCrudEventDriven</p>
      </div>
    </aside>
  )
}
