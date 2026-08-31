import { Link } from 'react-router-dom'
import { Scoreboard } from '../game/Scoreboard'
import type { PlayerView } from '../../types/game'

interface ResultsViewProps {
  solutionWord: string
  winners: PlayerView[]
  players: PlayerView[]
  meId: string | null
  yourFoundWords: string[]
}

export function ResultsView({ solutionWord, winners, players, meId, yourFoundWords }: ResultsViewProps) {
  const winnerNames = winners.map((w) => w.name).join(' & ')

  return (
    <div className="results-view">
      <h1>Time's up!</h1>
      <p className="solution-reveal">The word was</p>
      <p className="solution-word">{solutionWord}</p>
      {winnerNames && (
        <p className="winner-announcement">
          {winners.length > 1 ? `${winnerNames} tie for the win!` : `${winnerNames} wins!`}
        </p>
      )}

      {meId && yourFoundWords.length > 0 && (
        <div className="found-words">
          <h2>Your words ({yourFoundWords.length})</h2>
          <ul>
            {yourFoundWords.map((word) => (
              <li key={word}>{word}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="final-scores">
        <h2>Final Scores</h2>
        <Scoreboard players={players} meId={meId} />
      </div>

      <div className="results-actions">
        <Link to="/highscores" className="link">
          View High Scores
        </Link>
        <Link to="/" className="link">
          Back to Home
        </Link>
      </div>
    </div>
  )
}
