import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Save, Edit } from 'lucide-react'
import { getEmployee, createEmployee, updateEmployee } from '../api/employees'
import { FormField } from '../components/FormField'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { ErrorAlert } from '../components/ErrorAlert'
import type { EmployeeInfo, EmployeeDto } from '../types/employee'

function toDateInput(ddMMyyyy: string): string {
  if (!ddMMyyyy) return ''
  const [d, m, y] = ddMMyyyy.split('-')
  return `${y}-${m}-${d}`
}

function fromDateInput(yyyyMMdd: string): string {
  if (!yyyyMMdd) return ''
  const [y, m, d] = yyyyMMdd.split('-')
  return `${d}-${m}-${y}`
}

type Mode = 'create' | 'edit' | 'view'

const emptyForm: EmployeeInfo = {
  firstName: '',
  middleInitial: '',
  lastName: '',
  dateOfBirth: '',
  dateOfEmployment: '',
  status: 'ACTIVE',
}

export function EmployeeFormPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const mode: Mode = !id ? 'create' : location.pathname.endsWith('/edit') ? 'edit' : 'view'
  const isReadOnly = mode === 'view'

  const [form, setForm] = useState<EmployeeInfo>(emptyForm)
  const [loading, setLoading] = useState(mode !== 'create')
  const [saving, setSaving] = useState(false)
  const [fetchError, setFetchError] = useState<string | null>(null)
  const [validation, setValidation] = useState<Record<string, string>>({})

  useEffect(() => {
    if (!id || mode === 'create') return
    setLoading(true)
    getEmployee(id)
      .then((data: EmployeeDto) => {
        setForm({
          firstName: data.firstName,
          middleInitial: data.middleInitial,
          lastName: data.lastName,
          dateOfBirth: toDateInput(data.dateOfBirth),
          dateOfEmployment: toDateInput(data.dateOfEmployment),
          status: data.status,
        })
      })
      .catch(() => setFetchError('Failed to load employee'))
      .finally(() => setLoading(false))
  }, [id, mode])

  const set = (field: keyof EmployeeInfo, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }))
    setValidation((prev) => ({ ...prev, [field]: '' }))
  }

  const validate = (): boolean => {
    const errs: Record<string, string> = {}
    if (!form.firstName.trim()) errs.firstName = 'First name is required'
    if (!form.lastName.trim()) errs.lastName = 'Last name is required'
    if (form.middleInitial.length > 1) errs.middleInitial = 'Max 1 character'
    if (!form.dateOfBirth.trim()) errs.dateOfBirth = 'Date of birth is required'
    if (!form.dateOfEmployment.trim()) errs.dateOfEmployment = 'Date of employment is required'
    setValidation(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    setSaving(true)
    try {
      const payload = {
        ...form,
        dateOfBirth: fromDateInput(form.dateOfBirth),
        dateOfEmployment: fromDateInput(form.dateOfEmployment),
      }
      if (mode === 'create') {
        await createEmployee(payload)
      } else if (id) {
        await updateEmployee(id, { ...payload, id })
      }
      navigate('/employees')
    } catch {
      setValidation({ _form: 'Failed to save employee. Please try again.' })
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (fetchError) return <ErrorAlert message={fetchError} onRetry={() => navigate('/employees')} />

  const title = mode === 'create' ? 'New Employee' : mode === 'edit' ? 'Edit Employee' : 'Employee Details'

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/employees')} className="rounded p-1 text-gray-500 hover:bg-gray-100">
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-semibold text-gray-900">{title}</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-gray-200 bg-white p-6">
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

          <FormField label="Middle Initial" error={validation.middleInitial}>
            <input
              type="text"
              value={form.middleInitial}
              onChange={(e) => set('middleInitial', e.target.value.toUpperCase())}
              readOnly={isReadOnly}
              maxLength={1}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
            />
          </FormField>
        </div>

        <FormField label="Last Name" required error={validation.lastName}>
          <input
            type="text"
            value={form.lastName}
            onChange={(e) => set('lastName', e.target.value)}
            readOnly={isReadOnly}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
          />
        </FormField>

        <div className="grid grid-cols-2 gap-4">
          <FormField label="Date of Birth" required error={validation.dateOfBirth}>
            <input
              type="date"
              value={form.dateOfBirth}
              onChange={(e) => set('dateOfBirth', e.target.value)}
              readOnly={isReadOnly}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
            />
          </FormField>

          <FormField label="Date of Employment" required error={validation.dateOfEmployment}>
            <input
              type="date"
              value={form.dateOfEmployment}
              onChange={(e) => set('dateOfEmployment', e.target.value)}
              readOnly={isReadOnly}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 read-only:bg-gray-50 read-only:text-gray-500"
            />
          </FormField>
        </div>

        <FormField label="Status" required>
          <select
            value={form.status}
            onChange={(e) => set('status', e.target.value)}
            disabled={isReadOnly}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 disabled:bg-gray-50 disabled:text-gray-500"
          >
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </FormField>

        {validation._form && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{validation._form}</p>
        )}

        {isReadOnly && id && (
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate(`/employees/${id}/edit`)}
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
              onClick={() => navigate('/employees')}
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
