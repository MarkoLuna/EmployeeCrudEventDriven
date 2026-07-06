import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { DataTable, type Column } from '../src/components/DataTable'

interface Item {
  id: string
  name: string
}

const columns: Column<Item>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: 'Name' },
]

const data: Item[] = [
  { id: '1', name: 'Alice' },
  { id: '2', name: 'Bob' },
]

describe('DataTable', () => {
  it('shows loading spinner when loading', () => {
    render(
      <DataTable columns={columns} data={[]} keyExtractor={(i) => i.id} loading />,
    )
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('shows empty state when no data', () => {
    render(
      <DataTable columns={columns} data={[]} keyExtractor={(i) => i.id} />,
    )
    expect(screen.getByText('No data found')).toBeInTheDocument()
  })

  it('shows error message and retry button', () => {
    const onRetry = vi.fn()
    render(
      <DataTable
        columns={columns}
        data={[]}
        keyExtractor={(i) => i.id}
        error="Something went wrong"
        onRetry={onRetry}
      />,
    )
    expect(screen.getByText('Something went wrong')).toBeInTheDocument()
    expect(screen.getByText('Retry')).toBeInTheDocument()
  })

  it('renders table rows for data', () => {
    render(
      <DataTable columns={columns} data={data} keyExtractor={(i) => i.id} />,
    )
    expect(screen.getByText('Alice')).toBeInTheDocument()
    expect(screen.getByText('Bob')).toBeInTheDocument()
    expect(screen.getByText('ID')).toBeInTheDocument()
    expect(screen.getByText('Name')).toBeInTheDocument()
  })

  it('renders action buttons when actions prop is provided', () => {
    render(
      <DataTable
        columns={columns}
        data={data}
        keyExtractor={(i) => i.id}
        actions={(item) => <button onClick={() => {}}>Edit {item.name}</button>}
      />,
    )
    expect(screen.getByText('Edit Alice')).toBeInTheDocument()
    expect(screen.getByText('Edit Bob')).toBeInTheDocument()
  })
})
