import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { login as apiLogin } from '../api/auth'
import { clearTokens, getStoredTokens } from '../api/client'
import { decodeToken, extractUser, isTokenExpired } from '../utils/jwt'
import type { AuthUser } from '../types/auth'

interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  hasAnyRole: (roles: string[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUserFromStorage)
  const navigate = useNavigate()

  const logout = useCallback(() => {
    clearTokens()
    setUser(null)
    navigate('/login')
  }, [navigate])

  useEffect(() => {
    const handler = () => logout()
    window.addEventListener('auth:expired', handler)
    return () => window.removeEventListener('auth:expired', handler)
  }, [logout])

  const login = useCallback(async (username: string, password: string) => {
    const tokenResponse = await apiLogin(username, password)
    const decoded = decodeToken(tokenResponse.access_token)
    if (!decoded) throw new Error('Failed to decode token')
    const authUser = extractUser(decoded)
    setUser(authUser)
  }, [])

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

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}
