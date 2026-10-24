import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getHighScores } from '../api/highscores'
import { getDailyHistory, getDailyLeaderboard } from '../api/daily'
import { LeaderboardTable } from '../components/shared/LeaderboardTable'
import type { DailyHistoryEntryView, LeaderboardQuery } from '../types/game'

type Tab = 'all-time' | 'daily' | 'previous'

function isTab(value: string | null): value is Tab {
  return value === 'all-time' || value === 'daily' || value === 'previous'
}

function getTodaysDailyLeaderboard(query: LeaderboardQuery) {
  return getDailyLeaderboard(undefined, query)
}

/**
 * Daily dates are plain UTC calendar days ("2026-10-23"). `new Date("2026-10-23")` would parse
 * that as UTC midnight and show the previous day anywhere west of UTC, so read it as local midnight.
 */
function formatChallengeDate(date: string): string {
  return new Date(`${date}T00:00:00`).toLocaleDateString()
}

export function HighScoresPage() {
  const [searchParams] = useSearchParams()
  const [tab, setTab] = useState<Tab>(() => {
    const requested = searchParams.get('tab')
    return isTab(requested) ? requested : 'all-time'
  })
  const [allowRepeatPlayers, setAllowRepeatPlayers] = useState(true)

  const [history, setHistory] = useState<DailyHistoryEntryView[] | null>(null)
  const [historyError, setHistoryError] = useState<string | null>(null)
  const [expandedDate, setExpandedDate] = useState<string | null>(null)

  useEffect(() => {
    if (tab === 'previous' && history === null) {
      setHistoryError(null)
      getDailyHistory()
        .then(setHistory)
        .catch((err) => setHistoryError(err instanceof Error ? err.message : 'Failed to load previous dailies'))
    }
    // Only fetched once, the first time the tab is opened.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab])

  const uniquePlayers = !allowRepeatPlayers

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

      <label className="leaderboard-option">
        <input
          type="checkbox"
          checked={allowRepeatPlayers}
          onChange={(e) => setAllowRepeatPlayers(e.target.checked)}
        />
        Allow the same player to appear more than once
      </label>

      {tab === 'all-time' && <LeaderboardTable key="all-time" load={getHighScores} uniquePlayers={uniquePlayers} />}

      {tab === 'daily' && (
        <LeaderboardTable
          key="daily"
          load={getTodaysDailyLeaderboard}
          uniquePlayers={uniquePlayers}
          emptyMessage="No one has finished today's Daily Wang yet — be the first!"
        />
      )}

      {tab === 'previous' && (
        historyError ? (
          <p className="form-error">{historyError}</p>
        ) : history === null ? (
          <p>Loading…</p>
        ) : history.length === 0 ? (
          <p>No previous dailies yet.</p>
        ) : (
          <ul className="daily-history-list">
            {history.map((entry) => (
              <li key={entry.date}>
                <button
                  type="button"
                  className="daily-history-row"
                  onClick={() => setExpandedDate(expandedDate === entry.date ? null : entry.date)}
                >
                  {formatChallengeDate(entry.date)} — {entry.solutionWord}
                </button>
                {expandedDate === entry.date && (
                  <LeaderboardTable
                    key={entry.date}
                    load={(query) => getDailyLeaderboard(entry.date, query)}
                    uniquePlayers={uniquePlayers}
                  />
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
