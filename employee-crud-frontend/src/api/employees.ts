import { employeeApiClient } from './client'
import type { EmployeeDto, EmployeeInfo, EmployeePage } from '../types/employee'

export async function listEmployees(page: number = 0, size: number = 10): Promise<EmployeePage> {
  const response = await employeeApiClient.get<EmployeePage>(`/employees/${page}/${size}`)
  return response.data
}

export async function getEmployee(id: string): Promise<EmployeeDto> {
  const response = await employeeApiClient.get<EmployeeDto>(`/employees/${id}`)
  return response.data
}

export async function createEmployee(data: EmployeeInfo): Promise<EmployeeDto> {
  const response = await employeeApiClient.post<EmployeeDto>('/employees', data)
  return response.data
}

export async function updateEmployee(id: string, data: EmployeeDto): Promise<EmployeeDto> {
  const response = await employeeApiClient.put<EmployeeDto>(`/employees/${id}`, data)
  return response.data
}

export async function deleteEmployee(id: string): Promise<void> {
  await employeeApiClient.delete(`/employees/${id}`)
}
