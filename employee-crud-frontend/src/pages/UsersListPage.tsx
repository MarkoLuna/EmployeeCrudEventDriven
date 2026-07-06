import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Edit, Trash2, RefreshCw } from 'lucide-react'
import { useUsers } from '../hooks/useUsers'
import { DataTable, type Column } from '../components/DataTable'
import { Pagination } from '../components/Pagination'
import { ConfirmDialog } from '../components/ConfirmDialog'
import type { UserResponse } from '../types/user'

const columns: Column<UserResponse>[] = [
  { key: 'username', header: 'Username' },
  { key: 'firstName', header: 'First Name' },
  { key: 'lastName', header: 'Last Name' },
  { key: 'email', header: 'Email' },
  {
    key: 'enabled',
    header: 'Enabled',
    render: (u) => (u.enabled
      ? <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-800">Yes</span>
      : <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600">No</span>),
  },
]

export function UsersListPage() {
  const navigate = useNavigate()
  const { users, page, totalPages, loading, error, fetchUsers, remove } = useUsers(10)
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  useEffect(() => {
    fetchUsers(0)
  }, [fetchUsers])

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
        <h1 className="text-2xl font-semibold text-gray-900">Users</h1>
        <div className="flex gap-2">
          <button
            onClick={() => fetchUsers(page)}
            className="flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"
          >
            <RefreshCw size={16} />
            Refresh
          </button>
          <button
            onClick={() => navigate('/users/new')}
            className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-3 py-2 text-sm text-white hover:bg-indigo-700"
          >
            <Plus size={16} />
            New User
          </button>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={users}
        keyExtractor={(u) => u.id}
        loading={loading}
        error={error}
        onRetry={() => fetchUsers(page)}
        emptyTitle="No users found"
        emptyDescription="Create your first user to get started."
        actions={(u) => (
          <div className="flex justify-end gap-1">
            <button
              onClick={() => navigate(`/users/${u.id}/edit`)}
              className="rounded p-1.5 text-gray-500 hover:bg-gray-100 hover:text-indigo-600"
              title="Edit"
            >
              <Edit size={15} />
            </button>
            <button
              onClick={() => setDeleteId(u.id)}
              className="rounded p-1.5 text-gray-500 hover:bg-gray-100 hover:text-red-600"
              title="Delete"
            >
              <Trash2 size={15} />
            </button>
          </div>
        )}
      />

      <Pagination page={page} totalPages={totalPages} onPageChange={fetchUsers} />

      <ConfirmDialog
        open={!!deleteId}
        title="Delete User"
        message="Are you sure you want to delete this user? This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteId(null)}
        loading={deleteLoading}
      />
    </div>
  )
}
