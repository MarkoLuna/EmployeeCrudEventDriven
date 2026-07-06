import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { ProtectedRoute } from '../src/components/ProtectedRoute'

vi.mock('../src/contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}))

import { useAuth } from '../src/contexts/AuthContext'
const mockUseAuth = useAuth as ReturnType<typeof vi.fn>

function renderWithRouter(initialEntry: string, children: React.ReactNode) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/" element={<div>Home Page</div>} />
        <Route path="/protected" element={children} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('redirects to /login when not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      hasAnyRole: () => false,
    })

    renderWithRouter(
      '/protected',
      <ProtectedRoute><div>Protected Content</div></ProtectedRoute>,
    )

    expect(screen.getByText('Login Page')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })

  it('renders children when authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      hasAnyRole: () => true,
    })

    renderWithRouter(
      '/protected',
      <ProtectedRoute><div>Protected Content</div></ProtectedRoute>,
    )

    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })

  it('redirects to / when user lacks required role', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      hasAnyRole: () => false,
    })

    renderWithRouter(
      '/protected',
      <ProtectedRoute requiredRoles={['admin']}>
        <div>Admin Content</div>
      </ProtectedRoute>,
    )

    expect(screen.getByText('Home Page')).toBeInTheDocument()
    expect(screen.queryByText('Admin Content')).not.toBeInTheDocument()
  })
})
