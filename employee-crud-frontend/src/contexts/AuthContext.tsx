import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { login as apiLogin } from '../api/auth'
import { clearTokens, getStoredTokens } from '../api/client'
import { decodeToken, extractUser, isTokenExpired } from '../utils/jwt'
import type { AuthUser } from '../types/auth'

/**
 * Interface for authentication context
 * @property {AuthUser | null} user - Authenticated user
 * @property {boolean} isAuthenticated - Whether the user is authenticated
 * @property {function(string, string): Promise<void>} login - Logs in the user
 * @property {function(): void} logout - Logs out the user
 * @property {function(string[]): boolean} hasAnyRole - Checks if the user has any of the specified roles
 */
interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  hasAnyRole: (roles: string[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

/**
 * Loads user from storage
 * @returns {AuthUser | null}
 */
function loadUserFromStorage(): AuthUser | null {
  const { accessToken } = getStoredTokens()
  if (!accessToken) return null
  if (isTokenExpired(accessToken)) {
    clearTokens()
    return null
  }
  const decoded = decodeToken(accessToken)
  if (!decoded) {
    clearTokens()
    return null
  }
  return extractUser(decoded)
}

/**
 * Provider for authentication
 * @param {ReactNode} children - Children to render
 * @returns {ReactNode}
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUserFromStorage)
  const navigate = useNavigate()

  /**
   * Logs out the user
   */
  const logout = useCallback(() => {
    clearTokens()
    setUser(null)
    navigate('/login')
  }, [navigate])

  useEffect(() => {
    /**
     * Handles token expiration
     */
    const handler = () => logout()
    window.addEventListener('auth:expired', handler)
    return () => window.removeEventListener('auth:expired', handler)
  }, [logout])

  /**
   * Logs in the user
   * @param {string} username - Username
   * @param {string} password - Password
   * @returns {Promise<void>}
   */
  const login = useCallback(async (username: string, password: string) => {
    const tokenResponse = await apiLogin(username, password)
    const decoded = decodeToken(tokenResponse.access_token)
    if (!decoded) throw new Error('Failed to decode token')
    const authUser = extractUser(decoded)
    setUser(authUser)
  }, [])

  /**
   * Checks if the user has any of the specified roles
   * @param {string[]} roles - Roles to check
   * @returns {boolean}
   */
  const hasAnyRole = useCallback(
    (roles: string[]) => {
      if (!user) return false
      return roles.some((role) => user.roles.includes(role))
    },
    [user],
  )

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout, hasAnyRole }}>
      {children}
    </AuthContext.Provider>
  )
}

/**
 * Hook for authentication
 * @returns {AuthContextValue}
 */
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}
