export interface Credential {
  type: string
  value: string
  temporary: boolean
}

export interface UserCreateRequest {
  username: string
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  emailVerified: boolean
  credentials: Credential[]
}

export interface UserResponse {
  id: string
  username: string
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  emailVerified: boolean
  createdTimestamp: number
  realmRoles: string[]
  clientRoles: Record<string, string[]>
  attributes: Record<string, string>
}

export interface UserUpdateRequest {
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  emailVerified: boolean
  credentials?: Credential[]
  attributes: Record<string, string>
}

export interface UserPage {
  pageNumber: number
  pageSize: number
  offset: number
  content: UserResponse[]
}
