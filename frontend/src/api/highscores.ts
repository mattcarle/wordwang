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

/** All-time rank of the score a player recorded in a game. Rejects (404) if none was recorded. */
export async function getHighScoreRank(gameId: string, playerName: string): Promise<number> {
  const params = new URLSearchParams({ gameId, playerName })
  const result = await get<{ rank: number }>(`/api/highscores/rank?${params}`)
  return result.rank
}

export function getHighScores(query: LeaderboardQuery): Promise<LeaderboardPageView> {
  return get<LeaderboardPageView>(`/api/highscores?${leaderboardParams(query)}`)
}
