import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useState } from 'react'
import { getJoinableGames } from '../api/games'
import type { JoinableGameView } from '../types/game'

// See useGameSocket.ts for why the SockJS URL carries the Vite base path.
const WS_BASE = import.meta.env.BASE_URL.replace(/\/$/, '')

/**
 * Live list of games waiting for players. Returns null until the first list arrives. The backend
 * pushes the full list on /topic/joinable-games whenever a game is created, joined, started or
 * quit; the list is also re-fetched on every (re)connect so nothing is missed between the initial
 * fetch and the subscription, or while the socket was down.
 */
export function useJoinableGames(enabled: boolean): JoinableGameView[] | null {
  const [games, setGames] = useState<JoinableGameView[] | null>(null)

  useEffect(() => {
    if (!enabled) {
      return
    }
    let cancelled = false
    setGames(null)

    const refresh = () => {
      getJoinableGames()
        .then((list) => {
          if (!cancelled) setGames(list)
        })
        .catch(() => {
          if (!cancelled) setGames((prev) => prev ?? [])
        })
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
      reconnectDelay: 2000,
    })
    client.onConnect = () => {
      client.subscribe('/topic/joinable-games', (message) => {
        if (!cancelled) setGames(JSON.parse(message.body) as JoinableGameView[])
      })
      refresh()
    }
    client.activate()
    refresh()

    return () => {
      cancelled = true
      client.deactivate()
    }
  }, [enabled])

  return games
}
