import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider, useAuth } from '../src/contexts/AuthContext'

vi.mock('../src/api/auth', () => ({
  login: vi.fn(),
}))

vi.mock('../src/api/client', () => ({
  clearTokens: vi.fn(),
  getStoredTokens: vi.fn(() => ({ accessToken: null, refreshToken: null })),
  dispatchAuthExpired: vi.fn(),
  storeTokens: vi.fn(),
  authClient: {},
}))

import { login as loginApi } from '../src/api/auth'

function TestComponent() {
  const { user, isAuthenticated, login, logout, hasAnyRole } = useAuth()
  return (
    <div>
      <p data-testid="auth-status">{isAuthenticated ? 'logged-in' : 'logged-out'}</p>
      {user && <p data-testid="user-email">{user.email}</p>}
      {user && <p data-testid="user-roles">{user.roles.join(',')}</p>}
      <button data-testid="login-btn" onClick={() => login('john@test.com', '123')}>
        Login
      </button>
      <button data-testid="logout-btn" onClick={logout}>
        Logout
      </button>
      <p data-testid="has-role">{hasAnyRole(['manage-users']) ? 'yes' : 'no'}</p>
    </div>
  )
}

function renderWithProvider() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    </MemoryRouter>,
  )
}

const mockTokenResponse = {
  access_token:
    'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.' +
    btoa(JSON.stringify({
      sub: 'user-1',
      preferred_username: 'john',
      email: 'john@test.com',
      given_name: 'John',
      family_name: 'Doe',
      realm_access: { roles: ['offline_access'] },
      resource_access: { 'realm-management': { roles: ['manage-users', 'view-users'] } },
      exp: 9999999999,
      iat: 1000000000,
    })).replace(/\+/g, '-').replace(/\//g, '_') +
    '.signature',
  refresh_token: 'refresh-token',
  expires_in: 300,
  refresh_expires_in: 1800,
  token_type: 'Bearer',
  scope: 'openid profile roles',
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  afterEach(() => {
    localStorage.clear()
  })

  it('starts logged out when no token in storage', () => {
    renderWithProvider()
    expect(screen.getByTestId('auth-status').textContent).toBe('logged-out')
  })

  it('logs in successfully and shows user info', async () => {
    const user = userEvent.setup()
    vi.mocked(loginApi).mockResolvedValue(mockTokenResponse)

    renderWithProvider()
    await user.click(screen.getByTestId('login-btn'))

    await waitFor(() => {
      expect(screen.getByTestId('auth-status').textContent).toBe('logged-in')
    })
    expect(screen.getByTestId('user-email').textContent).toBe('john@test.com')
    expect(screen.getByTestId('user-roles').textContent).toContain('manage-users')
  })

  it('hasAnyRole returns correct value after login', async () => {
    const user = userEvent.setup()
    vi.mocked(loginApi).mockResolvedValue(mockTokenResponse)

    renderWithProvider()
    await user.click(screen.getByTestId('login-btn'))

    await waitFor(() => {
      expect(screen.getByTestId('has-role').textContent).toBe('yes')
    })
  })

  it('logs out and clears user', async () => {
    const user = userEvent.setup()
    vi.mocked(loginApi).mockResolvedValue(mockTokenResponse)

    renderWithProvider()
    await user.click(screen.getByTestId('login-btn'))

    await waitFor(() => {
      expect(screen.getByTestId('auth-status').textContent).toBe('logged-in')
    })

    await user.click(screen.getByTestId('logout-btn'))
    expect(screen.getByTestId('auth-status').textContent).toBe('logged-out')
  })
})
