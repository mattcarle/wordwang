import type { LeaderboardEntryView, LeaderboardSort, SortDirection } from '../../types/game'

export interface ScoreTableSort {
  column: LeaderboardSort
  direction: SortDirection
}

interface ScoreTableProps {
  rows: LeaderboardEntryView[]
  emptyMessage?: string
  sort: ScoreTableSort
  onSortChange: (sort: ScoreTableSort) => void
  /** Show each player's total game count - only meaningful when the board is grouped by player. */
  showCount?: boolean
  /** Show a Y/N column for Daily Wang scores - only the main board mixes daily and normal scores. */
  showDaily?: boolean
}

const COLUMNS: { column: LeaderboardSort; label: string; firstDirection: SortDirection }[] = [
  { column: 'rank', label: '#', firstDirection: 'asc' },
  { column: 'player', label: 'Player', firstDirection: 'asc' },
  { column: 'count', label: 'Count', firstDirection: 'desc' },
  { column: 'score', label: 'Score', firstDirection: 'desc' },
  { column: 'percent', label: '% of Max', firstDirection: 'desc' },
  { column: 'daily', label: 'Daily', firstDirection: 'desc' },
  { column: 'date', label: 'Date', firstDirection: 'desc' },
]

export function ScoreTable({
  rows,
  emptyMessage = 'No scores yet — be the first!',
  sort,
  onSortChange,
  showCount = false,
  showDaily = false,
}: ScoreTableProps) {
  if (rows.length === 0) {
    return <p>{emptyMessage}</p>
  }

  const columns = COLUMNS.filter(
    (c) => (c.column !== 'count' || showCount) && (c.column !== 'daily' || showDaily),
  )

  // Clicking a new column starts it in its most useful direction (best first, A-Z, newest first);
  // clicking the active column again flips it.
  function handleHeaderClick(column: LeaderboardSort, firstDirection: SortDirection) {
    if (sort.column === column) {
      onSortChange({ column, direction: sort.direction === 'asc' ? 'desc' : 'asc' })
    } else {
      onSortChange({ column, direction: firstDirection })
    }
  }

  return (
    <table className="highscore-table">
      <thead>
        <tr>
          {columns.map(({ column, label, firstDirection }) => {
            const active = sort.column === column
            return (
              <th
                key={column}
                aria-sort={active ? (sort.direction === 'asc' ? 'ascending' : 'descending') : 'none'}
              >
                <button
                  type="button"
                  className={`sort-btn${active ? ' sort-btn-active' : ''}`}
                  onClick={() => handleHeaderClick(column, firstDirection)}
                >
                  {label}
                  <span className="sort-indicator" aria-hidden="true">
                    {active ? (sort.direction === 'asc' ? '▲' : '▼') : ''}
                  </span>
                </button>
              </th>
            )
          })}
        </tr>
      </thead>
      <tbody>
        {rows.map((entry, index) => (
          <tr key={`${entry.playerName}-${entry.date}-${index}`}>
            <td>{entry.rank}</td>
            <td>{entry.playerName}</td>
            {showCount && <td>{entry.gameCount}</td>}
            <td>{entry.score}</td>
            <td>{entry.percentOfMaxPossible !== null ? `${entry.percentOfMaxPossible}%` : '—'}</td>
            {showDaily && <td>{entry.daily ? 'Y' : 'N'}</td>}
            <td>{new Date(entry.date).toLocaleDateString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
