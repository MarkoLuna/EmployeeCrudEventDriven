import { describe, it, expect } from 'vitest'
import { decodeToken, extractUser, hasAnyRole } from '../src/utils/jwt'
import type { DecodedToken, AuthUser } from '../src/types/auth'

function makeToken(payload: Record<string, unknown>): string {
  const b64 = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_')
  return `header.${b64}.signature`
}

describe('decodeToken', () => {
  it('returns null for invalid token', () => {
    expect(decodeToken('not-a-token')).toBeNull()
  })

  it('decodes a valid token', () => {
    const token = makeToken({ sub: 'abc', preferred_username: 'john' })
    const decoded = decodeToken(token)
    expect(decoded).not.toBeNull()
    expect(decoded!.sub).toBe('abc')
    expect(decoded!.preferred_username).toBe('john')
  })
})

describe('extractUser', () => {
  it('collects roles from resource_access', () => {
    const decoded: DecodedToken = {
      sub: 'id-1',
      preferred_username: 'john',
      email: 'john@test.com',
      given_name: 'John',
      family_name: 'Doe',
      realm_access: { roles: ['offline_access'] },
      resource_access: {
        'realm-management': { roles: ['manage-users', 'view-users'] },
        account: { roles: ['manage-account'] },
      },
      exp: 9999999999,
      iat: 1000000000,
    }
    const user: AuthUser = extractUser(decoded)
    expect(user.id).toBe('id-1')
    expect(user.username).toBe('john')
    expect(user.email).toBe('john@test.com')
    expect(user.roles).toEqual(['manage-users', 'view-users', 'manage-account'])
  })

  it('handles empty resource_access', () => {
    const decoded: DecodedToken = {
      sub: 'id-2',
      preferred_username: 'mike',
      email: 'mike@other.com',
      given_name: 'Mike',
      family_name: '',
      realm_access: { roles: [] },
      resource_access: {},
      exp: 9999999999,
      iat: 1000000000,
    }
    const user = extractUser(decoded)
    expect(user.roles).toEqual([])
  })
})

describe('hasAnyRole', () => {
  const user: AuthUser = {
    id: '1',
    username: 'john',
    email: 'john@test.com',
    firstName: '',
    lastName: '',
    roles: ['manage-users', 'view-users'],
  }

  it('returns true when user has one of the required roles', () => {
    expect(hasAnyRole(user, ['view-users', 'admin'])).toBe(true)
  })

  it('returns false when user has none of the required roles', () => {
    expect(hasAnyRole(user, ['admin', 'manage-clients'])).toBe(false)
  })

  it('returns false for empty required roles list', () => {
    expect(hasAnyRole(user, [])).toBe(false)
  })
})
