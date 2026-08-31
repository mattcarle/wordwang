import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useEffect, useRef, useState } from 'react'
import type { GameEvent } from '../types/game'

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
      webSocketFactory: () => new SockJS('/ws'),
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
