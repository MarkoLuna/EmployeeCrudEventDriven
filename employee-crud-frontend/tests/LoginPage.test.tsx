import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from '../src/contexts/AuthContext'
import { LoginPage } from '../src/pages/LoginPage'

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

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    </MemoryRouter>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('renders the login form with required fields', () => {
    renderLoginPage()

    expect(screen.getByPlaceholderText('john@test.com')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument()
  })

  it('shows error when submitting with empty fields', async () => {
    const user = userEvent.setup()
    renderLoginPage()

    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(screen.getByText('Username and password are required')).toBeInTheDocument()
  })

  it('shows error on invalid credentials', async () => {
    const user = userEvent.setup()
    vi.mocked(loginApi).mockRejectedValue(new Error('Unauthorized'))

    renderLoginPage()

    await user.type(screen.getByPlaceholderText('john@test.com'), 'wrong@test.com')
    await user.type(screen.getByPlaceholderText('••••••'), 'wrongpass')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    await waitFor(() => {
      expect(screen.getByText('Invalid username or password')).toBeInTheDocument()
    })
  })
})
