import { useEffect, useState } from 'react'
import { User, Save, Edit, X } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { FormField } from '../components/FormField'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { getCurrentUser, updateCurrentUser } from '../api/users'

export function UserProfilePage() {
  const { user } = useAuth()
  const [userData, setUserData] = useState<{
    firstName: string
    lastName: string
    email: string
  } | null>(null)
  const [editing, setEditing] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [editForm, setEditForm] = useState({ firstName: '', lastName: '', email: '' })
  const [error, setError] = useState<string | null>(null)
  const [validation, setValidation] = useState<Record<string, string>>({})

  useEffect(() => {
    if (!user) return
    getCurrentUser()
      .then((data) => {
        setUserData({
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
        })
      })
      .catch(() => {
        setUserData({ firstName: user.firstName, lastName: user.lastName, email: user.email })
      })
      .finally(() => setLoading(false))
  }, [user])

  const startEditing = () => {
    if (!userData) return
    setEditForm({ firstName: userData.firstName || '', lastName: userData.lastName || '', email: userData.email || '' })
    setEditing(true)
    setError(null)
    setValidation({})
  }

  const cancelEditing = () => {
    setEditing(false)
    setError(null)
    setValidation({})
  }

  const validate = (): boolean => {
    const errs: Record<string, string> = {}
    if (!editForm.firstName.trim()) errs.firstName = 'First name is required'
    if (!editForm.lastName.trim()) errs.lastName = 'Last name is required'
    if (!editForm.email.trim()) errs.email = 'Email is required'
    setValidation(errs)
    return Object.keys(errs).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    setSaving(true)
    setError(null)
    try {
      await updateCurrentUser({
        firstName: editForm.firstName.trim(),
        lastName: editForm.lastName.trim(),
        email: editForm.email.trim(),
        enabled: true,
        emailVerified: false,
        attributes: {},
      })
      setUserData({
        firstName: editForm.firstName.trim(),
        lastName: editForm.lastName.trim(),
        email: editForm.email.trim(),
      })
      setEditing(false)
    } catch {
      setError('Failed to update profile. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  if (!user) return null

  if (loading) return <LoadingSpinner />

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100">
          <User className="text-indigo-600" size={20} />
        </div>
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">My Profile</h1>
          <p className="text-sm text-gray-500">{user.email}</p>
        </div>
      </div>

      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <div className="space-y-4">
          <FormField label="Username">
            <p className="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-700">
              {user.username}
            </p>
          </FormField>

          {editing ? (
            <>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="First Name" required error={validation.firstName}>
                  <input
                    type="text"
                    value={editForm.firstName}
                    onChange={(e) => {
                      setEditForm((p) => ({ ...p, firstName: e.target.value }))
                      setValidation((p) => ({ ...p, firstName: '' }))
                    }}
                    className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  />
                </FormField>
                <FormField label="Last Name" required error={validation.lastName}>
                  <input
                    type="text"
                    value={editForm.lastName}
                    onChange={(e) => {
                      setEditForm((p) => ({ ...p, lastName: e.target.value }))
                      setValidation((p) => ({ ...p, lastName: '' }))
                    }}
                    className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  />
                </FormField>
              </div>
              <FormField label="Email" required error={validation.email}>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => {
                    setEditForm((p) => ({ ...p, email: e.target.value }))
                    setValidation((p) => ({ ...p, email: '' }))
                  }}
                  className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                />
              </FormField>
            </>
          ) : (
            <>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="First Name">
                  <p className="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-700">
                    {userData?.firstName || user.firstName || '—'}
                  </p>
                </FormField>
                <FormField label="Last Name">
                  <p className="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-700">
                    {userData?.lastName || user.lastName || '—'}
                  </p>
                </FormField>
              </div>
              <FormField label="Email">
                <p className="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-700">
                  {userData?.email ?? user.email}
                </p>
              </FormField>
            </>
          )}

          <FormField label="Roles">
            <div className="flex flex-wrap gap-1.5">
              {user.roles.length > 0
                ? user.roles.map((role) => (
                    <span
                      key={role}
                      className="rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-medium text-indigo-700"
                    >
                      {role}
                    </span>
                  ))
                : <span className="text-sm text-gray-400">No assigned roles</span>}
            </div>
          </FormField>

          {error && (
            <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
          )}

          <div className="flex justify-end gap-3 pt-2">
            {editing ? (
              <>
                <button
                  type="button"
                  onClick={cancelEditing}
                  className="flex items-center gap-1.5 rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <X size={16} />
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={saving}
                  className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700 disabled:opacity-50"
                >
                  <Save size={16} />
                  {saving ? 'Saving...' : 'Save'}
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={startEditing}
                className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
              >
                <Edit size={16} />
                Edit
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
