import { get, post } from './client'
import type {
  CreateGameResponse,
  GameSnapshotResponse,
  GameSummaryResponse,
  JoinableGameView,
  JoinGameResponse,
  StartGameResponse,
} from '../types/game'

export function createGame(organiserName: string): Promise<CreateGameResponse> {
  return post<CreateGameResponse>('/api/games', { organiserName })
}

export function getJoinableGames(): Promise<JoinableGameView[]> {
  return get<JoinableGameView[]>('/api/games/joinable')
}

export function getGameSummary(gameId: string): Promise<GameSummaryResponse> {
  return get<GameSummaryResponse>(`/api/games/${gameId}`)
}

export function getGameSnapshot(gameId: string, playerId?: string): Promise<GameSnapshotResponse> {
  const query = playerId ? `?playerId=${encodeURIComponent(playerId)}` : ''
  return get<GameSnapshotResponse>(`/api/games/${gameId}/snapshot${query}`)
}

export function joinGame(gameId: string, playerName: string): Promise<JoinGameResponse> {
  return post<JoinGameResponse>(`/api/games/${gameId}/join`, { playerName })
}

export function startGame(gameId: string, playerId: string): Promise<StartGameResponse> {
  return post<StartGameResponse>(`/api/games/${gameId}/start`, { playerId })
}

export function quitGame(gameId: string, playerId: string): Promise<void> {
  return post<void>(`/api/games/${gameId}/quit`, { playerId })
}
