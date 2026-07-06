import { useState, useCallback } from 'react'
import * as usersApi from '../api/users'
import type { UserCreateRequest, UserResponse, UserUpdateRequest, UserPage } from '../types/user'

interface UseUsersReturn {
  users: UserResponse[]
  page: number
  totalPages: number
  loading: boolean
  error: string | null
  fetchUsers: (pageNum?: number) => Promise<void>
  create: (data: UserCreateRequest) => Promise<void>
  update: (id: string, data: UserUpdateRequest) => Promise<void>
  remove: (id: string) => Promise<void>
}

export function useUsers(pageSize: number = 10): UseUsersReturn {
  const [users, setUsers] = useState<UserResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchUsers = useCallback(async (pageNum: number = 0) => {
    setLoading(true)
    setError(null)
    try {
      const result: UserPage = await usersApi.listUsers(pageNum, pageSize)
      setUsers(result.content)
      setPage(result.pageNumber)
      setTotalPages(Math.max(1, Math.ceil((result.offset || 1) / result.pageSize)))
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to fetch users'
      setError(message)
    } finally {
      setLoading(false)
    }
  }, [pageSize])

  const create = useCallback(async (data: UserCreateRequest) => {
    await usersApi.createUser(data)
    await fetchUsers(0)
  }, [fetchUsers])

  const update = useCallback(async (id: string, data: UserUpdateRequest) => {
    await usersApi.updateUser(id, data)
    await fetchUsers(page)
  }, [fetchUsers, page])

  const remove = useCallback(async (id: string) => {
    await usersApi.deleteUser(id)
    await fetchUsers(page)
  }, [fetchUsers, page])

  return {
    users,
    page,
    totalPages,
    loading,
    error,
    fetchUsers,
    create,
    update,
    remove,
  }
}
