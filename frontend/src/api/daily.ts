import { get, post } from './client'
import type { CreateGameResponse, DailyChallengeView, DailyHistoryEntryView, DailyInfoView } from '../types/game'

export function getDailyInfo(): Promise<DailyInfoView> {
  return get<DailyInfoView>('/api/daily')
}

export function getDailyLeaderboard(date?: string, limit = 20): Promise<DailyChallengeView[]> {
  const params = new URLSearchParams({ limit: String(limit) })
  if (date) params.set('date', date)
  return get<DailyChallengeView[]>(`/api/daily/leaderboard?${params}`)
}

export function getDailyHistory(limit = 30): Promise<DailyHistoryEntryView[]> {
  return get<DailyHistoryEntryView[]>(`/api/daily/history?limit=${limit}`)
}

export function createDailyGame(organiserName: string, dailyPlayerId: string): Promise<CreateGameResponse> {
  return post<CreateGameResponse>('/api/games/daily', { organiserName, dailyPlayerId })
}
