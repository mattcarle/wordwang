import { get, post } from './client'
import { leaderboardParams } from './highscores'
import type {
  CreateGameResponse,
  DailyHistoryEntryView,
  DailyInfoView,
  LeaderboardPageView,
  LeaderboardQuery,
} from '../types/game'

export function getDailyInfo(): Promise<DailyInfoView> {
  return get<DailyInfoView>('/api/daily')
}

/** Omitting `date` means today's (UTC) Daily Wang. */
export function getDailyLeaderboard(date: string | undefined, query: LeaderboardQuery): Promise<LeaderboardPageView> {
  const params = leaderboardParams(query)
  if (date) params.set('date', date)
  return get<LeaderboardPageView>(`/api/daily/leaderboard?${params}`)
}

export function getDailyHistory(limit = 30): Promise<DailyHistoryEntryView[]> {
  return get<DailyHistoryEntryView[]>(`/api/daily/history?limit=${limit}`)
}

export function createDailyGame(organiserName: string, dailyPlayerId: string): Promise<CreateGameResponse> {
  return post<CreateGameResponse>('/api/games/daily', { organiserName, dailyPlayerId })
}
