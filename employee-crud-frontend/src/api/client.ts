import axios from 'axios'

const AUTH_URL = import.meta.env.VITE_AUTH_URL
const EMPLOYEE_API_URL = import.meta.env.VITE_EMPLOYEE_API_URL
const USERS_API_URL = import.meta.env.VITE_USERS_API_URL
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID
const CLIENT_SECRET = import.meta.env.VITE_CLIENT_SECRET

let refreshingPromise: Promise<boolean> | null = null

function getStoredTokens() {
  const accessToken = localStorage.getItem('access_token')
  const refreshToken = localStorage.getItem('refresh_token')
  return { accessToken, refreshToken }
}

function storeTokens(access: string, refresh: string) {
  localStorage.setItem('access_token', access)
  localStorage.setItem('refresh_token', refresh)
}

function clearTokens() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('auth_user')
}

export function dispatchAuthExpired() {
  window.dispatchEvent(new CustomEvent('auth:expired'))
}

async function refreshTokens(): Promise<boolean> {
  const { refreshToken } = getStoredTokens()
  if (!refreshToken) return false

  try {
    const params = new URLSearchParams({
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
    })

    const response = await axios.post(
      `${AUTH_URL}/protocol/openid-connect/token`,
      params.toString(),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } },
    )

    storeTokens(response.data.access_token, response.data.refresh_token)
    return true
  } catch {
    clearTokens()
    dispatchAuthExpired()
    return false
  }
}

export const authClient = axios.create({
  baseURL: AUTH_URL,
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
})

function createApiClient(baseURL: string) {
  const client = axios.create({ baseURL })

  client.interceptors.request.use((config) => {
    const { accessToken } = getStoredTokens()
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  })

  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        if (!refreshingPromise) {
          refreshingPromise = refreshTokens()
        }

        const refreshed = await refreshingPromise
        refreshingPromise = null

        if (refreshed) {
          const { accessToken } = getStoredTokens()
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return client(originalRequest)
        }
      }

      return Promise.reject(error)
    },
  )

  return client
}

export const employeeApiClient = createApiClient(EMPLOYEE_API_URL)
export const usersApiClient = createApiClient(USERS_API_URL)

export { CLIENT_ID, CLIENT_SECRET, AUTH_URL, storeTokens, clearTokens, getStoredTokens }
