import { usersApiClient } from './client'
import type { UserCreateRequest, UserResponse, UserUpdateRequest, UserPage } from '../types/user'

export async function listUsers(page: number = 0, size: number = 10): Promise<UserPage> {
  const response = await usersApiClient.get<UserPage>(`/users/${page}/${size}`)
  return response.data
}

export async function getUser(id: string): Promise<UserResponse> {
  const response = await usersApiClient.get<UserResponse>(`/users/${id}`)
  return response.data
}

export async function getUserByUsername(username: string): Promise<UserResponse> {
  const response = await usersApiClient.get<UserResponse>(`/users/username/${username}`)
  return response.data
}

export async function createUser(data: UserCreateRequest): Promise<void> {
  await usersApiClient.post('/users', data)
}

export async function updateUser(id: string, data: UserUpdateRequest): Promise<void> {
  await usersApiClient.put(`/users/${id}`, data)
}

export async function getCurrentUser(): Promise<UserResponse> {
  const response = await usersApiClient.get<UserResponse>('/users/me')
  return response.data
}

export async function updateCurrentUser(data: UserUpdateRequest): Promise<void> {
  await usersApiClient.put('/users/me', data)
}

export async function deleteUser(id: string): Promise<void> {
  await usersApiClient.delete(`/users/${id}`)
}
