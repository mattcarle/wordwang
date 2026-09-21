const DAILY_PLAYER_COOKIE = 'wordwang_daily_player'
const ONE_YEAR_SECONDS = 60 * 60 * 24 * 365

/**
 * A persistent, anonymous identity for this browser, used only to let the server enforce "one
 * Daily Wang per day" - separate from the per-game player id in playerIdentity.ts, which resets
 * for every game. Created once and reused indefinitely.
 */
export function getOrCreateDailyPlayerId(): string {
  const match = document.cookie.match(new RegExp(`(?:^|; )${DAILY_PLAYER_COOKIE}=([^;]*)`))
  if (match) return decodeURIComponent(match[1])

  const id = crypto.randomUUID()
  document.cookie = `${DAILY_PLAYER_COOKIE}=${encodeURIComponent(id)}; max-age=${ONE_YEAR_SECONDS}; path=/; samesite=lax`
  return id
}
