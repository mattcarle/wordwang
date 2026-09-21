import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Scoreboard } from '../game/Scoreboard'
import { ScoreTable, type ScoreTableRow } from '../shared/ScoreTable'
import { getDailyLeaderboard } from '../../api/daily'
import { saveDailyCompletion } from '../../utils/dailyCompletion'
import { playApplauseSound, playGameEndSound } from '../../utils/sound'
import type { PlayerView } from '../../types/game'

interface ResultsViewProps {
  solutionWord: string
  winners: PlayerView[]
  players: PlayerView[]
  meId: string | null
  yourFoundWords: string[]
  maxPossibleScore: number
  dailyChallengeDate?: string | null
}

export function ResultsView({
  solutionWord,
  winners,
  players,
  meId,
  yourFoundWords,
  maxPossibleScore,
  dailyChallengeDate,
}: ResultsViewProps) {
  const winnerNames = winners.map((w) => w.name).join(' & ')
  const isWinner = meId !== null && winners.some((w) => w.playerId === meId)
  const yourScore = players.find((p) => p.playerId === meId)?.score
  const winningScore = winners[0]?.score
  const yourPercentOfMax =
    yourScore !== undefined && maxPossibleScore > 0 ? Math.round((yourScore / maxPossibleScore) * 100) : undefined

  const [dailyLeaderboard, setDailyLeaderboard] = useState<ScoreTableRow[] | null>(null)

  useEffect(() => {
    if (isWinner) {
      playApplauseSound()
    } else {
      playGameEndSound()
    }
    // Runs once when the results screen first mounts - re-running on prop changes isn't wanted.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (!dailyChallengeDate || yourScore === undefined) return

    saveDailyCompletion({
      date: dailyChallengeDate,
      score: yourScore,
      maxPossibleScore,
      solutionWord,
      foundWords: yourFoundWords,
    })

    getDailyLeaderboard(dailyChallengeDate)
      .then((entries) =>
        setDailyLeaderboard(
          entries.map((entry) => ({
            playerName: entry.playerName,
            score: entry.score,
            percentOfMaxPossible: entry.percentOfMaxPossible,
            date: entry.completedAt,
          })),
        ),
      )
      .catch(() => setDailyLeaderboard([]))
    // Only needs to run once, when a daily game's result screen first mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dailyChallengeDate])

  return (
    <div className="results-view">
      <h1>Time's up!</h1>
      <p className="solution-reveal">The word was</p>
      <p className="solution-word">{solutionWord}</p>
      {winnerNames && (
        <p className="winner-announcement">
          {isWinner
            ? 'Congratulations, you win!'
            : winners.length > 1
              ? `${winnerNames} tie for the win!`
              : `${winnerNames} wins!`}
        </p>
      )}

      {meId && yourScore !== undefined && winningScore !== undefined && (
        <p className="score-summary">
          <span>
            Your score: <strong>{yourScore}</strong>
          </span>
          <span>
            Winning score: <strong>{winningScore}</strong>
          </span>
          <span>
            Max possible: <strong>{maxPossibleScore}</strong>
          </span>
          {yourPercentOfMax !== undefined && (
            <span>
              Percent of possible points earned: <strong>{yourPercentOfMax}%</strong>
            </span>
          )}
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

      {dailyChallengeDate && (
        <div className="daily-results-leaderboard">
          <h2>Today's Daily Leaderboard</h2>
          {dailyLeaderboard === null ? <p>Loading…</p> : <ScoreTable rows={dailyLeaderboard} />}
        </div>
      )}

      <div className="final-scores">
        <h2>Final Scores</h2>
        <Scoreboard players={players} meId={meId} />
      </div>

      <div className="results-actions">
        <Link to={dailyChallengeDate ? '/highscores?tab=daily' : '/highscores'} className="link">
          View High Scores
        </Link>
        <Link to="/" className="link">
            ← Back to Home
        </Link>
      </div>
    </div>
  )
}
