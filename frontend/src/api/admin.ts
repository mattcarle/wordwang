import { del, get, post } from './client'
import type { AuditPageResponse } from '../types/admin'

export function getSetupStatus(): Promise<{ setupRequired: boolean }> {
  return get('/api/admin/setup-status')
}

export function setupAdmin(password: string): Promise<void> {
  return post('/api/admin/setup', { password })
}

export function login(password: string): Promise<void> {
  return post('/api/admin/login', { password })
}

export function logout(): Promise<void> {
  return post('/api/admin/logout')
}

export function getMe(): Promise<void> {
  return get('/api/admin/me')
}

export function clearHighScores(): Promise<void> {
  return del('/api/admin/highscores')
}

export interface AuditQuery {
  page: number
  size: number
  from?: string
  to?: string
}

export function getAuditTrail({ page, size, from, to }: AuditQuery): Promise<AuditPageResponse> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (from) params.set('from', from)
  if (to) params.set('to', to)
  return get(`/api/admin/audit?${params.toString()}`)
}
