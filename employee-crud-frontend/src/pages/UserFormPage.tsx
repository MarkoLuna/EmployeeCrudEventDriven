import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Save, Edit } from 'lucide-react'
import { getUser, createUser, updateUser } from '../api/users'
import { FormField } from '../components/FormField'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { ErrorAlert } from '../components/ErrorAlert'
import type { UserCreateRequest, UserResponse, UserUpdateRequest } from '../types/user'

type Mode = 'create' | 'edit' | 'view'

interface FormState {
  username: string
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  emailVerified: boolean
  password: string
}

const emptyForm: FormState = {
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  enabled: true,
  emailVerified: false,
  password: '',
}

export function UserFormPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const mode: Mode = !id ? 'create' : location.pathname.endsWith('/edit') ? 'edit' : 'view'
  const isReadOnly = mode === 'view'

  const [form, setForm] = useState<FormState>(emptyForm)
  const [loading, setLoading] = useState(mode !== 'create')
  const [saving, setSaving] = useState(false)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [validation, setValidation] = useState<Record<string, string>>({})

  useEffect(() => {
    if (!id || mode === 'create') return
    setLoading(true)
    getUser(id)
      .then((data: UserResponse) => {
        setForm({
          username: data.username,
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          enabled: data.enabled,
          emailVerified: data.emailVerified,
          password: '',
        })
      })
      .catch(() => setFetchError('Failed to load user'))
      .finally(() => setLoading(false))
  }, [id, mode])

  const set = (field: keyof FormState, value: string | boolean) => {
    setForm((prev) => ({ ...prev, [field]: value }))
    setValidation((prev) => ({ ...prev, [field]: '' }))
  }

  const validate = (): boolean => {
    const errs: Record<string, string> = {}
    if (!form.username.trim()) errs.username = 'Username is required'
    if (!form.firstName.trim()) errs.firstName = 'First name is required'
    if (!form.lastName.trim()) errs.lastName = 'Last name is required'
    if (!form.email.trim()) errs.email = 'Email is required'
    if (mode === 'create' && !form.password.trim()) errs.password = 'Password is required'
    setValidation(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    setSaving(true)
    try {
      if (mode === 'create') {
        const payload: UserCreateRequest = {
          username: form.username,
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          enabled: form.enabled,
          emailVerified: form.emailVerified,
          credentials: [{ type: 'password', value: form.password, temporary: false }],
        }
        await createUser(payload)
      } else if (id) {
        const payload: UserUpdateRequest = {
          firstName: form.firstName,
          lastName: form.lastName,
          email: form.email,
          enabled: form.enabled,
          emailVerified: form.emailVerified,
          attributes: {},
          ...(form.password.trim()
            ? { credentials: [{ type: 'password', value: form.password, temporary: false }] }
            : {}),
        }
        await updateUser(id, payload)
      }
      navigate('/users')
    } catch {
      setValidation({ _form: 'Failed to save user. Please try again.' })
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (fetchError) return <ErrorAlert message={fetchError} onRetry={() => navigate('/users')} />

  const title = mode === 'create' ? 'New User' : mode === 'edit' ? 'Edit User' : 'User Details'

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/users')} className="rounded p-1 text-gray-500 hover:bg-gray-100">
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-semibold text-gray-900">{title}</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-gray-200 bg-white p-6">
        <FormField label="Username" required error={validation.username}>
          <input
            type="text"
            value={form.username}
            onChange={(e) => set('username', e.target.value)}
            readOnly={isReadOnly || mode === 'edit'}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
          />
        </FormField>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="First Name" required error={validation.firstName}>
            <input
              type="text"
              value={form.firstName}
              onChange={(e) => set('firstName', e.target.value)}
              readOnly={isReadOnly}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
            />
          </FormField>

          <FormField label="Last Name" required error={validation.lastName}>
            <input
              type="text"
              value={form.lastName}
              onChange={(e) => set('lastName', e.target.value)}
              readOnly={isReadOnly}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
            />
          </FormField>
        </div>

        <FormField label="Email" required error={validation.email}>
          <input
            type="email"
            value={form.email}
            onChange={(e) => set('email', e.target.value)}
            readOnly={isReadOnly}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
          />
        </FormField>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="Enabled">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.enabled}
                onChange={(e) => set('enabled', e.target.checked)}
                disabled={isReadOnly}
                className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              />
              Account active
            </label>
          </FormField>

          <FormField label="Email Verified">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={form.emailVerified}
                onChange={(e) => set('emailVerified', e.target.checked)}
                disabled={isReadOnly}
                className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              />
              Verified
            </label>
          </FormField>
        </div>

        <FormField label={mode === 'create' ? 'Password' : 'New Password (leave blank to keep)'} required={mode === 'create'} error={validation.password}>
          <input
            type="password"
            value={form.password}
            onChange={(e) => set('password', e.target.value)}
            readOnly={isReadOnly}
            placeholder={mode === 'edit' ? 'Leave blank to keep current' : ''}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
          />
        </FormField>

        {validation._form && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{validation._form}</p>
        )}

        {isReadOnly && id && (
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate(`/users/${id}/edit`)}
              className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
            >
              <Edit size={16} />
              Edit
            </button>
          </div>
        )}

        {!isReadOnly && (
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate('/users')}
              className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              <Save size={16} />
              {saving ? 'Saving...' : mode === 'create' ? 'Create' : 'Update'}
            </button>
          </div>
        )}
      </form>
    </div>
  )
}
