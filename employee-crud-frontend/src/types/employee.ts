export type EmployeeStatus = 'ACTIVE' | 'INACTIVE'
export type SortDirection = 'ASCENDING' | 'DESCENDING'

export interface EmployeeDto {
  id: string
  firstName: string
  middleInitial: string
  lastName: string
  dateOfBirth: string
  dateOfEmployment: string
  status: EmployeeStatus
}

export interface EmployeeInfo {
  firstName: string
  middleInitial: string
  lastName: string
  dateOfBirth: string
  dateOfEmployment: string
  status: EmployeeStatus
}

export interface EmployeePage {
  pageNumber: number
  pageSize: number
  offset: number
  sort: SortDirection
  content: EmployeeDto[]
}
