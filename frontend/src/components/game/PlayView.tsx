import { useEffect, useState } from 'react'
import { CountdownTimer } from './CountdownTimer'
import { Scoreboard } from './Scoreboard'
import { TileInput } from './TileInput'
import type { GuessFeedbackEvent, PlayerView } from '../../types/game'

interface PlayViewProps {
  scrambledWord: string
  endsAt: string
  players: PlayerView[]
  meId: string | null
  isOrganiser: boolean
  yourFoundWords: string[]
  lastFeedback: GuessFeedbackEvent | null
  onSubmitGuess: (word: string) => void
  onQuit: () => void
  quitBusy?: boolean
  quitError?: string | null
}

export function PlayView({
  scrambledWord,
  endsAt,
  players,
  meId,
  isOrganiser,
  yourFoundWords,
  lastFeedback,
  onSubmitGuess,
  onQuit,
  quitBusy,
  quitError,
}: PlayViewProps) {
  const [toast, setToast] = useState<GuessFeedbackEvent | null>(null)

  useEffect(() => {
    if (lastFeedback && lastFeedback.playerId === meId) {
      setToast(lastFeedback)
      const timeout = setTimeout(() => setToast(null), 2000)
      return () => clearTimeout(timeout)
    }
  }, [lastFeedback, meId])

  return (
    <div className="play-view">
      <div className="play-header">
        <CountdownTimer endsAt={endsAt} />
      </div>

      <TileInput letters={scrambledWord} onSubmit={onSubmitGuess} />

      <div className={`feedback-toast${toast ? ' feedback-toast-visible' : ''} feedback-${toast?.outcome ?? ''}`}>
        {toast?.message ?? ''}
      </div>

      <div className="play-body">
        <div className="found-words">
          <h2>Your words ({yourFoundWords.length})</h2>
          <ul>
            {yourFoundWords.map((word) => (
              <li key={word}>{word}</li>
            ))}
          </ul>
        </div>

        <div className="live-scores">
          <h2>Scores</h2>
          <Scoreboard players={players} meId={meId} />
        </div>
      </div>

      {isOrganiser && (
        <div className="play-footer">
          {quitError && <p className="form-error">{quitError}</p>}
          <button type="button" className="btn btn-danger quit-game-btn" onClick={onQuit} disabled={quitBusy}>
            {quitBusy ? 'Ending…' : 'Quit Game'}
          </button>
        </div>
      )}
    </div>
  )
}
