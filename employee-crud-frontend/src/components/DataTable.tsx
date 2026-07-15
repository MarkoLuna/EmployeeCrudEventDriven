import type { ReactNode } from 'react'
import { LoadingSpinner } from './LoadingSpinner'
import { ErrorAlert } from './ErrorAlert'
import { EmptyState } from './EmptyState'

export interface Column<T> {
  key: string
  header: string
  render?: (item: T) => ReactNode
  className?: string
}

interface DataTableProps<T> {
  columns: Column<T>[]
  data: T[]
  keyExtractor: (item: T) => string
  loading?: boolean
  error?: string | null
  onRetry?: () => void
  emptyTitle?: string
  emptyDescription?: string
  actions?: (item: T) => ReactNode
}

/**
 * Data table component
 * @param {Column<T>} columns - Column definitions
 * @param {T[]} data - Data to display
 * @param {(item: T) => string} keyExtractor - Function to extract key
 * @param {boolean} loading - Whether data is loading
 * @param {string | null} error - Error message
 * @param {function(): void} onRetry - Callback for retry action
 * @param {string} emptyTitle - Title for empty state
 * @param {string | undefined} emptyDescription - Description for empty state
 * @param {(item: T) => ReactNode} actions - Actions to display
 * @returns {ReactNode}
 */
export function DataTable<T>({
  columns,
  data,
  keyExtractor,
  loading = false,
  error = null,
  onRetry,
  emptyTitle = 'No data found',
  emptyDescription,
  actions,
}: DataTableProps<T>) {
  if (loading) return <LoadingSpinner />

  if (error) return <ErrorAlert message={error} onRetry={onRetry} />

  if (data.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className={`px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 ${col.className ?? ''}`}
              >
                {col.header}
              </th>
            ))}
            {actions && (
              <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                Actions
              </th>
            )}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {data.map((item) => (
            <tr key={keyExtractor(item)} className="hover:bg-gray-50">
              {columns.map((col) => (
                <td
                  key={col.key}
                  className={`whitespace-nowrap px-4 py-3 text-sm text-gray-700 ${col.className ?? ''}`}
                >
                  {col.render ? col.render(item) : String(item[col.key as keyof T] ?? '')}
                </td>
              ))}
              {actions && (
                <td className="whitespace-nowrap px-4 py-3 text-right text-sm">
                  {actions(item)}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
