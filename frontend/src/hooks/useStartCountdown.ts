import { useEffect, useState } from 'react'
import { playCountdownTickSound, playStartGameJingle, START_JINGLE_DURATION_MS } from '../utils/sound'

export type CountdownDisplay = 3 | 2 | 1

const STEP_MS = 1000

/**
 * Plays the pre-game 3-2-1 countdown using fixed delays timed from the moment this client learns
 * the game is starting, rather than by comparing the server's absolute `countdownEndsAt` against
 * this device's own clock. That absolute-time approach broke down on a phone whose system clock
 * was a second or more off from the server's: the countdown would run fast or slow relative to
 * real time, sometimes never reaching "1" before the round actually started. Fixed local delays
 * sidestep clock skew entirely - the only thing that matters is that this device's own clock ticks
 * at the right rate, not what absolute time it thinks it is.
 *
 * The actual round start is still fully server-authoritative (the `GAME_STARTED` broadcast,
 * handled by `useGameState`/`GamePage`) - this hook only paces the cosmetic countdown leading up to
 * it. The server leaves enough headroom (see `GameService.START_COUNTDOWN`) for this fixed
 * sequence - the jingle, then a full three seconds - to finish right around when the round opens.
 */
export function useStartCountdown(countdownEndsAt: string | null): CountdownDisplay | null {
  const [display, setDisplay] = useState<CountdownDisplay | null>(null)

  useEffect(() => {
    if (!countdownEndsAt) return

    playStartGameJingle()

    const timers = ([3, 2, 1] as const).map((value, index) =>
      setTimeout(
        () => {
          setDisplay(value)
          playCountdownTickSound()
        },
        START_JINGLE_DURATION_MS + index * STEP_MS,
      ),
    )

    return () => {
      timers.forEach(clearTimeout)
    }
  }, [countdownEndsAt])

  return display
}
