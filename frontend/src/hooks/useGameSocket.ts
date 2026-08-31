import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useRef, useState } from 'react'
import type { GameEvent } from '../types/game'

// Derived from Vite's `base` (see vite.config.ts and api/client.ts) - this app is served from a
// path prefix (e.g. /wordwang/) behind the shared carle7-edge reverse proxy, not the domain root,
// so the SockJS negotiation requests (which are real HTTP requests) need that same prefix. The
// STOMP destinations used elsewhere in this file (/topic/..., /app/...) are logical channel names
// within the STOMP protocol itself, not URLs, so they're unaffected.
const WS_BASE = import.meta.env.BASE_URL.replace(/\/$/, '')

export function useGameSocket(gameId: string | undefined, onEvent: (event: GameEvent) => void) {
  const clientRef = useRef<Client | null>(null)
  const onEventRef = useRef(onEvent)
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    onEventRef.current = onEvent
  }, [onEvent])

  useEffect(() => {
    if (!gameId) {
      return
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE}/ws`),
      reconnectDelay: 2000,
    })

    client.onConnect = () => {
      setConnected(true)
      client.subscribe(`/topic/game/${gameId}`, (message) => {
        const event = JSON.parse(message.body) as GameEvent
        onEventRef.current(event)
      })
    }
    client.onWebSocketClose = () => setConnected(false)

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [gameId])

  function submitGuess(playerId: string, word: string) {
    if (!gameId || !clientRef.current?.connected) {
      return
    }
    clientRef.current.publish({
      destination: `/app/game/${gameId}/guess`,
      body: JSON.stringify({ playerId, word }),
    })
  }

  return { connected, submitGuess }
}
