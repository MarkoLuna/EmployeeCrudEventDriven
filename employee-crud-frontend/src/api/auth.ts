import { authClient, CLIENT_ID, CLIENT_SECRET, storeTokens } from './client'
import type { TokenResponse } from '../types/auth'

export async function login(username: string, password: string): Promise<TokenResponse> {
  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
    username,
    password,
    grant_type: 'password',
    scope: 'openid profile roles',
  })

  const response = await authClient.post<TokenResponse>(
    '/protocol/openid-connect/token',
    params.toString(),
  )

  storeTokens(response.data.access_token, response.data.refresh_token)
  return response.data
}
