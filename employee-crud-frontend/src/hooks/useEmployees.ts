import { useState, useCallback } from 'react'
import * as employeesApi from '../api/employees'
import type { EmployeeDto, EmployeeInfo, EmployeePage } from '../types/employee'

interface UseEmployeesReturn {
  employees: EmployeeDto[]
  page: number
  totalPages: number
  loading: boolean
  error: string | null
  fetchEmployees: (pageNum?: number) => Promise<void>
  create: (data: EmployeeInfo) => Promise<void>
  update: (id: string, data: EmployeeDto) => Promise<void>
  remove: (id: string) => Promise<void>
}

export function useEmployees(pageSize: number = 10): UseEmployeesReturn {
  const [employees, setEmployees] = useState<EmployeeDto[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchEmployees = useCallback(async (pageNum: number = 0) => {
    setLoading(true)
    setError(null)
    try {
      const result: EmployeePage = await employeesApi.listEmployees(pageNum, pageSize)
      setEmployees(result.content)
      setPage(result.pageNumber)
      setTotalPages(Math.max(1, Math.ceil((result.offset || 1) / result.pageSize)))
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to fetch employees'
      setError(message)
    } finally {
      setLoading(false)
    }
  }, [pageSize])

  const create = useCallback(async (data: EmployeeInfo) => {
    await employeesApi.createEmployee(data)
    await fetchEmployees(0)
  }, [fetchEmployees])

  const update = useCallback(async (id: string, data: EmployeeDto) => {
    await employeesApi.updateEmployee(id, data)
    await fetchEmployees(page)
  }, [fetchEmployees, page])

  const remove = useCallback(async (id: string) => {
    await employeesApi.deleteEmployee(id)
    await fetchEmployees(page)
  }, [fetchEmployees, page])

  return {
    employees,
    page,
    totalPages,
    loading,
    error,
    fetchEmployees,
    create,
    update,
    remove,
  }
}
