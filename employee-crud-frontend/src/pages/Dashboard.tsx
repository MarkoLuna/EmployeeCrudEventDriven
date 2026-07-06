import { Briefcase, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export function Dashboard() {
  const { user, hasAnyRole } = useAuth()
    const canManageUsers = hasAnyRole(['manage-users', 'view-users', 'query-users'])
    const employeeApiBase = import.meta.env.VITE_EMPLOYEE_API_URL || '/service'
    const usersApiBase = import.meta.env.VITE_USERS_API_URL || '/users'

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900">
          Welcome, {user?.firstName ?? user?.username}
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          EmployeeCrudEventDriven &middot; CQRS-lite + Event-Driven Architecture
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Link to="/employees" className="block rounded-lg border border-gray-200 bg-white p-5 hover:border-indigo-300 hover:shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-100">
              <Briefcase className="text-indigo-600" size={20} />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-700">Employees</p>
              <p className="text-xs text-gray-500">CRUD operations</p>
            </div>
          </div>
        </Link>

        {canManageUsers && (
          <Link to="/users" className="block rounded-lg border border-gray-200 bg-white p-5 hover:border-indigo-300 hover:shadow-sm">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-100">
                <Users className="text-indigo-600" size={20} />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-700">Users</p>
                <p className="text-xs text-gray-500">Identity &amp; access management</p>
              </div>
            </div>
          </Link>
        )}
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-5">
        <h2 className="mb-2 text-sm font-medium text-gray-700">Quick Links</h2>
        <ul className="space-y-1 text-sm text-indigo-600">
          <li><a href={`${employeeApiBase}/swagger-ui/index.html`} target="_blank" rel="noopener noreferrer" className="hover:underline">Employee API Docs</a></li>
          {canManageUsers && <li><a href={`${usersApiBase}/swagger-ui/index.html`} target="_blank" rel="noopener noreferrer" className="hover:underline">Users API Docs</a></li>}
        </ul>
      </div>
    </div>
  )
}
