import { ChevronLeft, ChevronRight } from 'lucide-react'

/**
 * Interface for pagination
 * @property {number} page - Current page
 * @property {number} totalPages - Total pages
 * @property {function(number): void} onPageChange - Callback for page change
 */
interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

/**
 * Pagination component
 * @param {number} page - Current page
 * @param {number} totalPages - Total pages
 * @param {function(number): void} onPageChange - Callback for page change
 * @returns {ReactNode}
 */
export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-center gap-2 py-4">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="rounded-md border border-gray-300 p-2 text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronLeft size={16} />
      </button>

      <span className="px-3 text-sm text-gray-700">
        Page {page + 1} of {totalPages}
      </span>

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="rounded-md border border-gray-300 p-2 text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronRight size={16} />
      </button>
    </div>
  )
}
