import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getHighScores } from '../api/highscores'
import { getDailyHistory, getDailyLeaderboard } from '../api/daily'
import { ScoreTable, type ScoreTableRow } from '../components/shared/ScoreTable'
import type { DailyChallengeView, DailyHistoryEntryView, HighScoreView } from '../types/game'

type Tab = 'all-time' | 'daily' | 'previous'

function isTab(value: string | null): value is Tab {
  return value === 'all-time' || value === 'daily' || value === 'previous'
}

function highScoreRows(scores: HighScoreView[]): ScoreTableRow[] {
  return scores.map((s) => ({
    playerName: s.playerName,
    score: s.score,
    percentOfMaxPossible: s.percentOfMaxPossible,
    date: s.playedAt,
  }))
}

function dailyRows(scores: DailyChallengeView[]): ScoreTableRow[] {
  return scores.map((s) => ({
    playerName: s.playerName,
    score: s.score,
    percentOfMaxPossible: s.percentOfMaxPossible,
    date: s.completedAt,
  }))
}

export function HighScoresPage() {
  const [searchParams] = useSearchParams()
  const [tab, setTab] = useState<Tab>(() => {
    const requested = searchParams.get('tab')
    return isTab(requested) ? requested : 'all-time'
  })

  const [allTimeScores, setAllTimeScores] = useState<HighScoreView[] | null>(null)
  const [dailyScores, setDailyScores] = useState<DailyChallengeView[] | null>(null)
  const [history, setHistory] = useState<DailyHistoryEntryView[] | null>(null)
  const [expanded, setExpanded] = useState<{ date: string; scores: DailyChallengeView[] | null } | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setError(null)
    if (tab === 'all-time' && allTimeScores === null) {
      getHighScores(20).then(setAllTimeScores).catch(reportError)
    }
    if (tab === 'daily' && dailyScores === null) {
      getDailyLeaderboard().then(setDailyScores).catch(reportError)
    }
    if (tab === 'previous' && history === null) {
      getDailyHistory().then(setHistory).catch(reportError)
    }
    // Only the active tab should trigger its own (once-only) fetch.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab])

  function reportError(err: unknown) {
    setError(err instanceof Error ? err.message : 'Failed to load scores')
  }

  function togglePreviousDay(date: string) {
    if (expanded?.date === date) {
      setExpanded(null)
      return
    }
    setExpanded({ date, scores: null })
    getDailyLeaderboard(date)
      .then((scores) => setExpanded({ date, scores }))
      .catch(reportError)
  }

  return (
    <main className="page">
      <h1>High Scores</h1>

      <div className="highscore-tabs">
        <button type="button" className={`tab-btn${tab === 'all-time' ? ' tab-btn-active' : ''}`} onClick={() => setTab('all-time')}>
          All-Time
        </button>
        <button type="button" className={`tab-btn${tab === 'daily' ? ' tab-btn-active' : ''}`} onClick={() => setTab('daily')}>
          Today's Daily
        </button>
        <button type="button" className={`tab-btn${tab === 'previous' ? ' tab-btn-active' : ''}`} onClick={() => setTab('previous')}>
          Previous Dailies
        </button>
      </div>

      {error && <p className="form-error">{error}</p>}

      {tab === 'all-time' && !error && (
        allTimeScores === null ? <p>Loading…</p> : <ScoreTable rows={highScoreRows(allTimeScores)} />
      )}

      {tab === 'daily' && !error && (
        dailyScores === null ? (
          <p>Loading…</p>
        ) : (
          <ScoreTable
            rows={dailyRows(dailyScores)}
            emptyMessage="No one has finished today's Daily Wang yet — be the first!"
          />
        )
      )}

      {tab === 'previous' && !error && (
        history === null ? (
          <p>Loading…</p>
        ) : history.length === 0 ? (
          <p>No previous dailies yet.</p>
        ) : (
          <ul className="daily-history-list">
            {history.map((entry) => (
              <li key={entry.date}>
                <button type="button" className="daily-history-row" onClick={() => togglePreviousDay(entry.date)}>
                  {new Date(entry.date).toLocaleDateString()} — {entry.solutionWord}
                </button>
                {expanded?.date === entry.date && (
                  expanded.scores === null ? <p>Loading…</p> : <ScoreTable rows={dailyRows(expanded.scores)} />
                )}
              </li>
            ))}
          </ul>
        )
      )}

      <Link to="/" className="link">
        ← Back to Home
      </Link>
    </main>
  )
}
