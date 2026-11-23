/**
 * Console timing for the end-of-game delay: shows how long after the round's `endsAt` the timer hit
 * 0, the server's GAME_ENDED message arrived, and the results screen rendered. Times are read from
 * the server-corrected clock (see serverClock.ts), so they mean the same thing whatever the
 * device's own clock says. Search the browser console for "[wordwang timing]".
 */
import { serverClockOffsetMs, serverNow } from './serverClock'

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
  console.info(
    `[wordwang timing] timer reached 0: ${sinceEnds(serverNow())} (device clock is ${-serverClockOffsetMs()}ms ahead of the server)`,
  )
}

export function logGameEndedReceived() {
  messageReceivedAt = serverNow()
  console.info(`[wordwang timing] GAME_ENDED message received: ${sinceEnds(messageReceivedAt)}`)
}

export function logResultsRendered() {
  const now = serverNow()
  const sinceMessage = messageReceivedAt === null ? 'n/a' : `${now - messageReceivedAt}ms after the message`
  console.info(`[wordwang timing] results screen rendered: ${sinceEnds(now)} (${sinceMessage})`)
}
