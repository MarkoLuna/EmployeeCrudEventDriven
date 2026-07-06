import type { DecodedToken, AuthUser } from '../types/auth'

export function decodeToken(token: string): DecodedToken | null {
  try {
    const payload = token.split('.')[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded) as DecodedToken
  } catch {
    return null
  }
}

export function isTokenExpired(token: string): boolean {
  const decoded = decodeToken(token)
  if (!decoded) return true
  return decoded.exp * 1000 < Date.now()
}

export function extractUser(decoded: DecodedToken): AuthUser {
  const roles = Object.values(decoded.resource_access ?? {})
    .flatMap(client => client.roles)

  return {
    id: decoded.sub,
    username: decoded.preferred_username,
    email: decoded.email,
    firstName: decoded.given_name,
    lastName: decoded.family_name,
    roles,
  }
}

export function hasAnyRole(user: AuthUser, requiredRoles: string[]): boolean {
  return requiredRoles.some(role => user.roles.includes(role))
}
