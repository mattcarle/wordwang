/**
 * Console timing for the end-of-game delay: shows, on the player's own clock, how long after the
 * round's `endsAt` the timer hit 0, the server's GAME_ENDED message arrived, and the results screen
 * rendered. Search the browser console for "[wordwang timing]". Clock differences between the
 * player's device and the server show up as a constant offset in all three numbers.
 */

let endsAtMs: number | null = null
let messageReceivedAt: number | null = null

function sinceEnds(now: number): string {
  return endsAtMs === null ? 'n/a' : `${now - endsAtMs}ms after endsAt`
}

export function noteRoundEndsAt(endsAt: string | null) {
  endsAtMs = endsAt ? new Date(endsAt).getTime() : null
  messageReceivedAt = null
}

export function logTimerReachedZero() {
  console.info(`[wordwang timing] timer reached 0: ${sinceEnds(Date.now())}`)
}

export function logGameEndedReceived() {
  messageReceivedAt = Date.now()
  console.info(`[wordwang timing] GAME_ENDED message received: ${sinceEnds(messageReceivedAt)}`)
}

export function logResultsRendered() {
  const now = Date.now()
  const sinceMessage = messageReceivedAt === null ? 'n/a' : `${now - messageReceivedAt}ms after the message`
  console.info(`[wordwang timing] results screen rendered: ${sinceEnds(now)} (${sinceMessage})`)
}
