/**
 * The server decides when a round ends (`endsAt` is a server timestamp), but a player's device
 * clock can easily be several seconds off - which made the on-screen timer hit 0 seconds before the
 * server actually ended the round, looking like a long delay before the results appeared. The
 * server therefore sends its own current time along with the round's data; we remember how far this
 * device's clock is from it and read "now" through `serverNow()`.
 *
 * The estimate is off by the message's one-way travel time (tens of milliseconds), which is fine
 * for a countdown.
 */

let offsetMs = 0

/** Call with the `serverTime` from a server message at the moment it is received. */
export function syncServerClock(serverTime: string | null | undefined) {
  if (!serverTime) return
  const server = new Date(serverTime).getTime()
  if (Number.isNaN(server)) return
  offsetMs = server - Date.now()
}

/** Milliseconds since the epoch according to the server's clock. */
export function serverNow(): number {
  return Date.now() + offsetMs
}

/** How far this device's clock is from the server's (positive = the server is ahead). */
export function serverClockOffsetMs(): number {
  return offsetMs
}
