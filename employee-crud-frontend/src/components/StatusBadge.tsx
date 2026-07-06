import type { EmployeeStatus } from '../types/employee'

const variants: Record<EmployeeStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-800',
  INACTIVE: 'bg-gray-100 text-gray-600',
}

export function StatusBadge({ status }: { status: EmployeeStatus }) {
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${variants[status]}`}>
      {status}
    </span>
  )
}
