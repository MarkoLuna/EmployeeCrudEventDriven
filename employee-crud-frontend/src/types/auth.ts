export interface LoginRequest {
  username: string
  password: string
}

export interface TokenResponse {
  access_token: string
  expires_in: number
  refresh_expires_in: number
  refresh_token: string
  token_type: string
  scope: string
}

export interface DecodedToken {
  sub: string
  preferred_username: string
  email: string
  given_name: string
  family_name: string
  realm_access: { roles: string[] }
  resource_access: Record<string, { roles: string[] }>
  exp: number
  iat: number
  [key: string]: unknown
}

export interface AuthUser {
  id: string
  username: string
  email: string
  firstName: string
  lastName: string
  roles: string[]
}
