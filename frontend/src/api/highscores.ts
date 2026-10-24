import { get } from './client'
import type { LeaderboardPageView, LeaderboardQuery } from '../types/game'

export function leaderboardParams(query: LeaderboardQuery): URLSearchParams {
  return new URLSearchParams({
    sort: query.sort,
    direction: query.direction,
    uniquePlayers: String(query.uniquePlayers),
    offset: String(query.offset),
    limit: String(query.limit),
  })
}

export function getHighScores(query: LeaderboardQuery): Promise<LeaderboardPageView> {
  return get<LeaderboardPageView>(`/api/highscores?${leaderboardParams(query)}`)
}
