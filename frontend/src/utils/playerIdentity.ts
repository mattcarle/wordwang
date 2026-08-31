function storageKey(gameId: string): string {
  return `wordwang_player_${gameId}`
}

export function getStoredPlayerId(gameId: string): string | null {
  return localStorage.getItem(storageKey(gameId))
}

export function storePlayerId(gameId: string, playerId: string): void {
  localStorage.setItem(storageKey(gameId), playerId)
}
