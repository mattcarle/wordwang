import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getHighScores } from '../api/highscores'
import type { HighScoreView } from '../types/game'

export function HighScoresPage() {
  const [scores, setScores] = useState<HighScoreView[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getHighScores(20)
      .then(setScores)
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load high scores'))
  }, [])

  return (
    <main className="page">
      <h1>High Scores</h1>
      {error && <p className="form-error">{error}</p>}
      {!error && !scores && <p>Loading…</p>}
      {scores && scores.length === 0 && <p>No games played yet — be the first!</p>}
      {scores && scores.length > 0 && (
        <table className="highscore-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Player</th>
              <th>Score</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {scores.map((entry, index) => (
              <tr key={`${entry.playerName}-${entry.playedAt}-${index}`}>
                <td>{index + 1}</td>
                <td>{entry.playerName}</td>
                <td>{entry.score}</td>
                <td>{new Date(entry.playedAt).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <Link to="/" className="link">
        ← Back to Home
      </Link>
    </main>
  )
}
