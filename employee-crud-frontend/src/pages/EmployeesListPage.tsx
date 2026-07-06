import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Edit, Trash2, RefreshCw } from 'lucide-react'
import { useEmployees } from '../hooks/useEmployees'
import { DataTable, type Column } from '../components/DataTable'
import { Pagination } from '../components/Pagination'
import { StatusBadge } from '../components/StatusBadge'
import { ConfirmDialog } from '../components/ConfirmDialog'
import type { EmployeeDto } from '../types/employee'

const columns: Column<EmployeeDto>[] = [
  { key: 'firstName', header: 'First Name' },
  { key: 'middleInitial', header: 'MI' },
  { key: 'lastName', header: 'Last Name' },
  { key: 'dateOfBirth', header: 'Date of Birth' },
  { key: 'dateOfEmployment', header: 'Date of Employment' },
  {
    key: 'status',
    header: 'Status',
    render: (e) => <StatusBadge status={e.status} />,
  },
]

export function EmployeesListPage() {
  const navigate = useNavigate()
  const { employees, page, totalPages, loading, error, fetchEmployees, remove } = useEmployees(10)
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    fetchEmployees(0)
  }, [fetchEmployees])

  const handleDelete = async () => {
    if (!deleteId) return
    setDeleteLoading(true)
    try {
      await remove(deleteId)
    } catch {
      /* error handled by hook */
    } finally {
      setDeleteLoading(false)
      setDeleteId(null)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-gray-900">Employees</h1>
        <div className="flex gap-2">
          <button
            onClick={() => fetchEmployees(page)}
            className="flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"
          >
            <RefreshCw size={16} />
            Refresh
          </button>
          <button
            onClick={() => navigate('/employees/new')}
            className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-3 py-2 text-sm text-white hover:bg-indigo-700"
          >
            <Plus size={16} />
            New Employee
          </button>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={employees}
        keyExtractor={(e) => e.id}
        loading={loading}
        error={error}
        onRetry={() => fetchEmployees(page)}
        emptyTitle="No employees found"
        emptyDescription="Create your first employee to get started."
        actions={(e) => (
          <div className="flex justify-end gap-1">
            <button
              onClick={() => navigate(`/employees/${e.id}/edit`)}
              className="rounded p-1.5 text-gray-500 hover:bg-gray-100 hover:text-indigo-600"
              title="Edit"
            >
              <Edit size={15} />
            </button>
            <button
              onClick={() => setDeleteId(e.id)}
              className="rounded p-1.5 text-gray-500 hover:bg-gray-100 hover:text-red-600"
              title="Delete"
            >
              <Trash2 size={15} />
            </button>
          </div>
        )}
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={fetchEmployees} />

      <ConfirmDialog
        open={!!deleteId}
        title="Delete Employee"
        message="Are you sure you want to delete this employee? This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteId(null)}
        loading={deleteLoading}
      />
    </div>
  )
}
