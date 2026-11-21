import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Scoreboard } from '../game/Scoreboard'
import { LeaderboardTable } from '../shared/LeaderboardTable'
import { getDailyLeaderboard } from '../../api/daily'
import { getPossibleWords } from '../../api/games'
import { saveDailyCompletion } from '../../utils/dailyCompletion'
import { playApplauseSound, playGameEndSound } from '../../utils/sound'
import type { PlayerView, PossibleWordsGroup } from '../../types/game'

interface ResultsViewProps {
  /** Needed to look up the possible words; without it (e.g. a saved daily result) the number isn't a link. */
  gameId?: string
  solutionWord: string
  winners: PlayerView[]
  players: PlayerView[]
  meId: string | null
  yourFoundWords: string[]
  maxPossibleScore: number
  dailyChallengeDate?: string | null
  singlePlayer?: boolean
}

export function ResultsView({
  gameId,
  solutionWord,
  winners,
  players,
  meId,
  yourFoundWords,
  maxPossibleScore,
  dailyChallengeDate,
  singlePlayer,
}: ResultsViewProps) {
  // "Winning score" is just your own score when playing solo (single player or the Daily Wang).
  const isSolo = singlePlayer || !!dailyChallengeDate
  const winnerNames = winners.map((w) => w.name).join(' & ')
  const isWinner = meId !== null && winners.some((w) => w.playerId === meId)
  const yourScore = players.find((p) => p.playerId === meId)?.score
  const winningScore = winners[0]?.score

  const [showPossible, setShowPossible] = useState(false)
  const [possibleWords, setPossibleWords] = useState<PossibleWordsGroup[] | null>(null)
  const [possibleError, setPossibleError] = useState<string | null>(null)

  function togglePossibleWords() {
    const next = !showPossible
    setShowPossible(next)
    if (next && possibleWords === null && gameId) {
      setPossibleError(null)
      getPossibleWords(gameId)
        .then(setPossibleWords)
        .catch((err) => setPossibleError(err instanceof Error ? err.message : 'Failed to load possible words'))
    }
  }

  const totalPossibleWords = possibleWords?.reduce((sum, g) => sum + g.words.length, 0) ?? 0
  const totalPossiblePoints = possibleWords?.reduce((sum, g) => sum + g.words.length * g.pointsPerWord, 0) ?? 0

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

    // Only needs to run once, when a daily game's result screen first mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dailyChallengeDate])

  return (
    <div className="results-view">
      <h1>Time's up!</h1>
      <p className="solution-reveal">The word was</p>
      <p className="solution-word">
        {solutionWord}{' '}
        <a
          className="solution-word-lookup"
          href={`https://www.dictionary.com/browse/${encodeURIComponent(solutionWord.toLowerCase())}`}
          target="_blank"
          rel="noreferrer"
          aria-label={`Look up ${solutionWord} on Dictionary.com`}
        >
          🔍
        </a>
      </p>
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
            Your score: <strong>{yourScore}</strong> (out of a possible{' '}
            {gameId ? (
              <button type="button" className="link-button" onClick={togglePossibleWords} aria-expanded={showPossible}>
                {maxPossibleScore}
              </button>
            ) : (
              maxPossibleScore
            )}
            )
          </span>
          {!isSolo && (
            <span>
              Winning score: <strong>{winningScore}</strong>
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

      {showPossible && (
        <div className="possible-words">
          {possibleError ? (
            <p className="form-error">{possibleError}</p>
          ) : possibleWords === null ? (
            <p>Loading…</p>
          ) : (
            <>
              <h2>
                All Possible Words ({totalPossibleWords} words for {totalPossiblePoints} points)
              </h2>
              {possibleWords.map((group) => (
                <div key={group.length} className="possible-words-group found-words">
                  <h3>
                    {group.length} letter words ({group.words.length} x {group.pointsPerWord} ={' '}
                    {group.words.length * group.pointsPerWord} points)
                  </h3>
                  <ul>
                    {group.words.map((word) => (
                      <li key={word}>{word}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </>
          )}
        </div>
      )}

      {dailyChallengeDate && (
        <div className="daily-results-leaderboard">
          <h2>Today's Daily Leaderboard</h2>
          <LeaderboardTable
            load={(query) => getDailyLeaderboard(dailyChallengeDate, query)}
            uniquePlayers={false}
          />
        </div>
      )}

      <div className="final-scores">
        <h2>Final Scores</h2>
        <Scoreboard players={players} meId={meId} />
      </div>

      <div className="results-actions">
        <Link to={dailyChallengeDate ? '/highscores?tab=daily' : '/highscores'} className="link">
          View Leaderboard
        </Link>
        <Link to="/" className="link">
            ← Back to Home
        </Link>
      </div>
    </div>
  )
}
