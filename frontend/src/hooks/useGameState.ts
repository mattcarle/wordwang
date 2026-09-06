import { useCallback, useEffect, useState } from 'react'
import { getGameSnapshot } from '../api/games'
import { useGameSocket } from './useGameSocket'
import type { GameEvent, GameStatus, GuessFeedbackEvent, PlayerView } from '../types/game'

export interface GameState {
  loading: boolean
  error: string | null
  status: GameStatus | null
  organiserId: string | null
  scrambledWord: string | null
  solutionWord: string | null
  countdownEndsAt: string | null
  endsAt: string | null
  players: PlayerView[]
  yourFoundWords: string[]
  winners: PlayerView[]
}

const initialState: GameState = {
  loading: true,
  error: null,
  status: null,
  organiserId: null,
  scrambledWord: null,
  solutionWord: null,
  countdownEndsAt: null,
  endsAt: null,
  players: [],
  yourFoundWords: [],
  winners: [],
}

export function useGameState(gameId: string | undefined, playerId: string | null) {
  const [state, setState] = useState<GameState>(initialState)
  const [lastFeedback, setLastFeedback] = useState<GuessFeedbackEvent | null>(null)

  useEffect(() => {
    if (!gameId) {
      return
    }
    let cancelled = false
    getGameSnapshot(gameId, playerId ?? undefined)
      .then((snapshot) => {
        if (cancelled) return
        setState({
          loading: false,
          error: null,
          status: snapshot.status,
          organiserId: snapshot.organiserId,
          scrambledWord: snapshot.scrambledWord,
          solutionWord: snapshot.solutionWord,
          countdownEndsAt: snapshot.countdownEndsAt,
          endsAt: snapshot.endsAt,
          players: snapshot.players,
          yourFoundWords: snapshot.yourFoundWords,
          winners: snapshot.winners,
        })
      })
      .catch((err) => {
        if (cancelled) return
        setState((prev) => ({ ...prev, loading: false, error: err.message ?? 'Failed to load game' }))
      })
    return () => {
      cancelled = true
    }
  }, [gameId, playerId])

  const handleEvent = useCallback(
    (event: GameEvent) => {
      switch (event.type) {
        case 'PLAYER_JOINED':
          setState((prev) => ({ ...prev, players: event.players }))
          break
        case 'GAME_STARTING':
          setState((prev) => ({ ...prev, status: 'STARTING', countdownEndsAt: event.countdownEndsAt }))
          break
        case 'GAME_STARTED':
          setState((prev) => ({
            ...prev,
            status: 'IN_PROGRESS',
            scrambledWord: event.scrambledWord,
            endsAt: event.endsAt,
            countdownEndsAt: null,
          }))
          break
        case 'SCORE_UPDATE':
          setState((prev) => ({ ...prev, players: event.players }))
          break
        case 'GUESS_FEEDBACK':
          setLastFeedback(event)
          if (event.playerId === playerId && event.outcome === 'VALID') {
            setState((prev) => ({ ...prev, yourFoundWords: [...prev.yourFoundWords, event.word] }))
          }
          break
        case 'GAME_ENDED':
          setState((prev) => ({
            ...prev,
            status: 'FINISHED',
            solutionWord: event.solutionWord,
            winners: event.winners,
            players: event.players,
          }))
          break
      }
    },
    [playerId],
  )

  const { submitGuess: publishGuess } = useGameSocket(gameId, handleEvent)

  const submitGuess = useCallback(
    (word: string) => {
      if (!playerId) return
      publishGuess(playerId, word)
    },
    [playerId, publishGuess],
  )

  return { state, submitGuess, lastFeedback }
}
