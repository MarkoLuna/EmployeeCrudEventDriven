import { BrowserRouter, Routes, Route, Outlet } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { NavMenu } from './components/NavMenu'
import { Header } from './components/Header'
import { Footer } from './components/Footer'
import { LoginPage } from './pages/LoginPage'
import { Dashboard } from './pages/Dashboard'
import { EmployeesListPage } from './pages/EmployeesListPage'
import { EmployeeFormPage } from './pages/EmployeeFormPage'
import { UsersListPage } from './pages/UsersListPage'
import { UserFormPage } from './pages/UserFormPage'
import { UserProfilePage } from './pages/UserProfilePage'
import { NotFoundPage } from './pages/NotFoundPage'

function Layout() {
  return (
    <div className="flex h-screen">
      <NavMenu />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="flex-1 overflow-auto px-6 py-6">
          <Outlet />
        </main>
        <Footer />
      </div>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Dashboard />} />
            <Route path="/employees" element={<EmployeesListPage />} />
            <Route path="/employees/new" element={<EmployeeFormPage />} />
            <Route path="/employees/:id" element={<EmployeeFormPage />} />
            <Route path="/employees/:id/edit" element={<EmployeeFormPage />} />

            <Route
              path="/users"
              element={
                <ProtectedRoute requiredRoles={['manage-users', 'view-users', 'query-users']}>
                  <UsersListPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users/new"
              element={
                <ProtectedRoute requiredRoles={['manage-users']}>
                  <UserFormPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users/:id"
              element={
                <ProtectedRoute requiredRoles={['manage-users', 'view-users', 'query-users']}>
                  <UserFormPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users/:id/edit"
              element={
                <ProtectedRoute requiredRoles={['manage-users']}>
                  <UserFormPage />
                </ProtectedRoute>
              }
            />

            <Route path="/profile" element={<UserProfilePage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
