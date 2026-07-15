import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import type { ReactNode } from 'react'

/**
 * Interface for protected route
 * @property {ReactNode} children - Children to render
 * @property {string[]} requiredRoles - Roles required to access the route
 */
interface ProtectedRouteProps {
  children: ReactNode
  requiredRoles?: string[]
}

/**
 * Protects a route
 * @param {ReactNode} children - Children to render
 * @param {string[]} requiredRoles - Roles required to access the route
 * @returns {ReactNode}
 */
export function ProtectedRoute({ children, requiredRoles }: ProtectedRouteProps) {
  const { isAuthenticated, hasAnyRole } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />

  if (requiredRoles && requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
