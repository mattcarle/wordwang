import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { joinGame, quitGame, startGame } from '../api/games'
import { LobbyView } from '../components/lobby/LobbyView'
import { PlayView } from '../components/game/PlayView'
import { ResultsView } from '../components/results/ResultsView'
import { useGameState } from '../hooks/useGameState'
import { useStartCountdown } from '../hooks/useStartCountdown'
import { saveName } from '../utils/cookies'
import { getStoredPlayerId, storePlayerId } from '../utils/playerIdentity'
import { playCountdownGoSound } from '../utils/sound'
import type { GameStatus } from '../types/game'

export function GamePage() {
  const { gameId } = useParams<{ gameId: string }>()
  const [meId, setMeId] = useState<string | null>(() => (gameId ? getStoredPlayerId(gameId) : null))
  const [joinBusy, setJoinBusy] = useState(false)
  const [joinError, setJoinError] = useState<string | null>(null)
  const [startBusy, setStartBusy] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)
  const [quitBusy, setQuitBusy] = useState(false)
  const [quitError, setQuitError] = useState<string | null>(null)

  const { state, submitGuess, lastFeedback } = useGameState(gameId, meId)
  const countdownStep = useStartCountdown(state.countdownEndsAt)

  const previousStatusRef = useRef<GameStatus | null>(null)
  useEffect(() => {
    // The actual round start is this transition, not any point in the local countdown display -
    // playing the "go" sound here (rather than at the end of the cosmetic 3-2-1) guarantees every
    // player hears it exactly when the round truly opens, regardless of their own countdown timing.
    if (previousStatusRef.current === 'STARTING' && state.status === 'IN_PROGRESS') {
      playCountdownGoSound()
    }
    previousStatusRef.current = state.status
  }, [state.status])

  if (!gameId) {
    return <main className="page">Invalid game link.</main>
  }

  async function handleJoin(name: string) {
    setJoinBusy(true)
    setJoinError(null)
    try {
      saveName(name)
      const joined = await joinGame(gameId!, name)
      storePlayerId(gameId!, joined.playerId)
      setMeId(joined.playerId)
    } catch (err) {
      setJoinError(err instanceof Error ? err.message : 'Failed to join game')
    } finally {
      setJoinBusy(false)
    }
  }

  async function handleStart() {
    if (!meId) return
    setStartBusy(true)
    setStartError(null)
    try {
      await startGame(gameId!, meId)
    } catch (err) {
      setStartError(err instanceof Error ? err.message : 'Failed to start game')
      setStartBusy(false)
    }
  }

  async function handleQuit() {
    if (!meId) return
    setQuitBusy(true)
    setQuitError(null)
    try {
      await quitGame(gameId!, meId)
    } catch (err) {
      setQuitError(err instanceof Error ? err.message : 'Failed to end game')
      setQuitBusy(false)
    }
  }

  if (state.loading) {
    return <main className="page">Loading…</main>
  }

  if (state.error || !state.status) {
    return (
      <main className="page">
        <p className="form-error">{state.error ?? 'Game not found'}</p>
        <Link to="/" className="link">
          ← Back to Home
        </Link>
      </main>
    )
  }

  if (state.status === 'LOBBY' || state.status === 'STARTING') {
    return (
      <main className="page">
        <LobbyView
          gameId={gameId}
          organiserName={state.players.find((p) => p.playerId === state.organiserId)?.name ?? ''}
          organiserId={state.organiserId ?? ''}
          players={state.players}
          meId={meId}
          onJoin={handleJoin}
          onStart={handleStart}
          joinBusy={joinBusy}
          joinError={joinError}
          startBusy={startBusy}
          startError={startError}
          countdownStep={countdownStep}
        />
      </main>
    )
  }

  if (state.status === 'IN_PROGRESS' && state.scrambledWord && state.endsAt) {
    return (
      <main className="page">
        <PlayView
          scrambledWord={state.scrambledWord}
          endsAt={state.endsAt}
          players={state.players}
          meId={meId}
          isOrganiser={meId !== null && meId === state.organiserId}
          yourFoundWords={state.yourFoundWords}
          lastFeedback={lastFeedback}
          onSubmitGuess={submitGuess}
          onQuit={handleQuit}
          quitBusy={quitBusy}
          quitError={quitError}
        />
      </main>
    )
  }

  if (state.status === 'FINISHED' && state.solutionWord) {
    return (
      <main className="page">
        <ResultsView
          solutionWord={state.solutionWord}
          winners={state.winners}
          players={state.players}
          meId={meId}
          yourFoundWords={state.yourFoundWords}
        />
      </main>
    )
  }

  return <main className="page">Loading…</main>
}
