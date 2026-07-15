import { Inbox } from 'lucide-react'
import type { ReactNode } from 'react'

/**
 * Interface for empty state
 * @property {ReactNode} icon - Icon to display
 * @property {string} title - Title to display
 * @property {string} description - Description to display
 * @property {ReactNode} action - Action to display
 */
interface EmptyStateProps {
  icon?: ReactNode
  title: string
  description?: string
  action?: ReactNode
}

/**
 * Empty state component
 * @param {ReactNode} icon - Icon to display
 * @param {string} title - Title to display
 * @param {string} description - Description to display
 * @param {ReactNode} action - Action to display
 * @returns {ReactNode}
 */
export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-3 py-12 text-center">
      {icon ?? <Inbox className="text-gray-400" size={48} />}
      <h3 className="text-lg font-medium text-gray-600">{title}</h3>
      {description && <p className="text-sm text-gray-500">{description}</p>}
      {action && <div className="mt-2">{action}</div>}
    </div>
  )
}
