import { useEffect, useRef, useState } from 'react'
import { ScoreTable, type ScoreTableSort } from './ScoreTable'
import type { LeaderboardEntryView, LeaderboardPageView, LeaderboardQuery } from '../../types/game'

const PAGE_SIZE = 20

/** The leaderboard's starting order - matches the backend's defaults. */
const DEFAULT_SORT: ScoreTableSort = { column: 'rank', direction: 'asc' }

interface LeaderboardTableProps {
  /** Read only on (re)fetch - give the component a different `key` to point it at another board. */
  load: (query: LeaderboardQuery) => Promise<LeaderboardPageView>
  uniquePlayers: boolean
  emptyMessage?: string
}

/**
 * A leaderboard that fetches its own pages: sorting happens server-side (so ranks and ordering
 * are over the whole board, not just what's loaded), and "Load more" appends the next page.
 */
export function LeaderboardTable({ load, uniquePlayers, emptyMessage }: LeaderboardTableProps) {
  const [sort, setSort] = useState<ScoreTableSort>(DEFAULT_SORT)
  const [entries, setEntries] = useState<LeaderboardEntryView[] | null>(null)
  const [hasMore, setHasMore] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  // Bumped whenever the sort or filter changes, so a response for a superseded query is dropped
  // rather than mixed into the new one.
  const generation = useRef(0)

  function query(offset: number): LeaderboardQuery {
    return { sort: sort.column, direction: sort.direction, uniquePlayers, offset, limit: PAGE_SIZE }
  }

  function reportError(err: unknown) {
    setError(err instanceof Error ? err.message : 'Failed to load scores')
  }

  useEffect(() => {
    const current = ++generation.current
    setEntries(null)
    setError(null)
    setLoadingMore(false)
    load(query(0))
      .then((page) => {
        if (current !== generation.current) return
        setEntries(page.entries)
        setHasMore(page.hasMore)
      })
      .catch((err) => current === generation.current && reportError(err))
    // `load` is deliberately excluded - callers remount via `key` to change board.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sort, uniquePlayers])

  function loadMore() {
    if (entries === null) return
    const current = generation.current
    setLoadingMore(true)
    load(query(entries.length))
      .then((page) => {
        if (current !== generation.current) return
        setEntries((prev) => [...(prev ?? []), ...page.entries])
        setHasMore(page.hasMore)
      })
      .catch((err) => current === generation.current && reportError(err))
      .finally(() => current === generation.current && setLoadingMore(false))
  }

  if (error && entries === null) return <p className="form-error">{error}</p>
  if (entries === null) return <p>Loading…</p>

  return (
    <div className="leaderboard-table">
      <ScoreTable rows={entries} emptyMessage={emptyMessage} sort={sort} onSortChange={setSort} />
      {error && <p className="form-error">{error}</p>}
      {hasMore && (
        <button type="button" className="btn btn-secondary load-more-btn" onClick={loadMore} disabled={loadingMore}>
          {loadingMore ? 'Loading…' : 'Load more'}
        </button>
      )}
    </div>
  )
}
