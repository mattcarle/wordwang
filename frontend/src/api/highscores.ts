import { get } from './client'
import type { HighScoreView } from '../types/game'

export function getHighScores(limit = 20): Promise<HighScoreView[]> {
  return get<HighScoreView[]>(`/api/highscores?limit=${limit}`)
}
