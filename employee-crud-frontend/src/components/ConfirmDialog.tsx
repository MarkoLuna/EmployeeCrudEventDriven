import { Modal } from './Modal'

/**
 * Interface for confirm dialog
 * @property {boolean} open - Whether the dialog is open
 * @property {string} title - Dialog title
 * @property {string} message - Dialog message
 * @property {string} confirmLabel - Confirm button label
 * @property {string} cancelLabel - Cancel button label
 * @property {function(): void} onConfirm - Callback for confirm action
 * @property {function(): void} onCancel - Callback for cancel action
 * @property {boolean} loading - Whether the dialog is loading
 */
interface ConfirmDialogProps {
  open: boolean
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  onConfirm: () => void
  onCancel: () => void
  loading?: boolean
}

/**
 * Confirm dialog component
 * @param {boolean} open - Whether the dialog is open
 * @param {string} title - Dialog title
 * @param {string} message - Dialog message
 * @param {string} confirmLabel - Confirm button label
 * @param {string} cancelLabel - Cancel button label
 * @param {function(): void} onConfirm - Callback for confirm action
 * @param {function(): void} onCancel - Callback for cancel action
 * @param {boolean} loading - Whether the dialog is loading
 * @returns {ReactNode}
 */
export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  onConfirm,
  onCancel,
  loading = false,
}: ConfirmDialogProps) {
  return (
    <Modal open={open} title={title} onClose={onCancel}>
      <p className="mb-6 text-sm text-gray-600">{message}</p>
      <div className="flex justify-end gap-3">
        <button
          onClick={onCancel}
          disabled={loading}
          className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50"
        >
          {cancelLabel}
        </button>
        <button
          onClick={onConfirm}
          disabled={loading}
          className="rounded-md bg-red-600 px-4 py-2 text-sm text-white hover:bg-red-700 disabled:opacity-50"
        >
          {loading ? 'Processing...' : confirmLabel}
        </button>
      </div>
    </Modal>
  )
}
