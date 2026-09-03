import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAuditTrail, getMe } from '../api/admin'
import type { AuditGameView } from '../types/admin'

const PAGE_SIZE = 20

type AuthState = 'loading' | 'unauthorized' | 'authorized'

function toStartOfDayIso(dateStr: string): string {
  return new Date(`${dateStr}T00:00:00`).toISOString()
}

function toEndOfDayIso(dateStr: string): string {
  return new Date(`${dateStr}T23:59:59.999`).toISOString()
}

// Formats/shifts dates via local Y/M/D components rather than toISOString(), which would
// re-interpret the result in UTC and can land on the wrong calendar day for the viewer's timezone.
function formatDateStr(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function todayDateStr(): string {
  return formatDateStr(new Date())
}

function shiftDate(dateStr: string, days: number): string {
  const [year, month, day] = dateStr.split('-').map(Number)
  const d = new Date(year, month - 1, day)
  d.setDate(d.getDate() + days)
  return formatDateStr(d)
}

export function AuditTrailPage() {
  const [authState, setAuthState] = useState<AuthState>('loading')
  const [games, setGames] = useState<AuditGameView[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [selectedDate, setSelectedDate] = useState(todayDateStr())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getMe()
      .then(() => setAuthState('authorized'))
      .catch(() => setAuthState('unauthorized'))
  }, [])

  const load = useCallback((date: string, targetPage: number) => {
    setLoading(true)
    setError(null)
    getAuditTrail({
      page: targetPage,
      size: PAGE_SIZE,
      from: toStartOfDayIso(date),
      to: toEndOfDayIso(date),
    })
      .then((response) => {
        setGames(response.games)
        setPage(response.page)
        setTotalPages(response.totalPages)
        setTotalElements(response.totalElements)
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load audit trail'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (authState === 'authorized') {
      load(selectedDate, 0)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authState, selectedDate])

  if (authState === 'loading') {
    return (
      <main className="page">
        <p>Loading…</p>
      </main>
    )
  }

  if (authState === 'unauthorized') {
    return (
      <main className="page">
        <h1>Audit Trail</h1>
        <p>You need to be logged in as admin to view this page.</p>
        <Link to="/admin" className="link">
          ← Go to Admin Login
        </Link>
      </main>
    )
  }

  const isToday = selectedDate >= todayDateStr()

  return (
    <main className="page audit-page">
      <h1>Audit Trail</h1>

      <div className="audit-filter">
        <input
          type="date"
          value={selectedDate}
          max={todayDateStr()}
          onChange={(e) => setSelectedDate(e.target.value)}
        />
        <button
          type="button"
          className="btn btn-secondary audit-day-nav-btn"
          onClick={() => setSelectedDate((d) => shiftDate(d, -1))}
          disabled={loading}
          aria-label="Previous day"
        >
          ←
        </button>
        <button
          type="button"
          className="btn btn-secondary audit-day-nav-btn"
          onClick={() => setSelectedDate((d) => shiftDate(d, 1))}
          disabled={isToday || loading}
          aria-label="Next day"
        >
          →
        </button>
      </div>

      {error && <p className="form-error">{error}</p>}
      {loading && <p>Loading…</p>}

      {!loading && !error && (
        <p className="audit-day-count">
          {totalElements} {totalElements === 1 ? 'game' : 'games'} on{' '}
          {new Date(`${selectedDate}T00:00:00`).toLocaleDateString()}
        </p>
      )}

      {!loading && !error && games.length === 0 && <p>No games found for this day.</p>}

      {!loading && games.length > 0 && (
        <div className="audit-games">
          {games.map((game) => (
            <div className="audit-game" key={game.gameCode}>
              <div className="audit-game-header">
                <span className="audit-game-code">Game {game.gameCode}</span>
                <span className="audit-game-date">{new Date(game.createdAt).toLocaleString()}</span>
                <span className="audit-game-word">{game.solutionWord}</span>
              </div>
              <div className="audit-players-table-wrap">
                <table className="audit-players-table">
                  <thead>
                    <tr>
                      <th>Player</th>
                      <th>Score</th>
                      <th>Found word</th>
                      <th>Winner</th>
                      <th>IP address</th>
                      <th>Location</th>
                    </tr>
                  </thead>
                  <tbody>
                    {game.players.map((player) => (
                      <tr key={player.name}>
                        <td>
                          {player.name}
                          {player.organiser && <span className="audit-organiser-tag">Organiser</span>}
                        </td>
                        <td>{player.score}</td>
                        <td>{player.foundEightLetterWord ? 'Yes' : 'No'}</td>
                        <td>{player.winner ? 'Yes' : 'No'}</td>
                        <td>{player.ipAddress ?? 'Unknown'}</td>
                        <td>{player.location}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="audit-pagination">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => load(selectedDate, page - 1)}
            disabled={page <= 0 || loading}
          >
            ← Previous
          </button>
          <span>
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => load(selectedDate, page + 1)}
            disabled={page >= totalPages - 1 || loading}
          >
            Next →
          </button>
        </div>
      )}

      <Link to="/admin" className="link admin-back-link">
        ← Back to Admin
      </Link>
    </main>
  )
}
