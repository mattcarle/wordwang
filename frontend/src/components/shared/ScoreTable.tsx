export interface ScoreTableRow {
  playerName: string
  score: number
  percentOfMaxPossible: number | null
  date: string
}

interface ScoreTableProps {
  rows: ScoreTableRow[]
  emptyMessage?: string
}

export function ScoreTable({ rows, emptyMessage = 'No scores yet — be the first!' }: ScoreTableProps) {
  if (rows.length === 0) {
    return <p>{emptyMessage}</p>
  }

  return (
    <table className="highscore-table">
      <thead>
        <tr>
          <th>#</th>
          <th>Player</th>
          <th>Score</th>
          <th>% of Max</th>
          <th>Date</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((entry, index) => (
          <tr key={`${entry.playerName}-${entry.date}-${index}`}>
            <td>{index + 1}</td>
            <td>{entry.playerName}</td>
            <td>{entry.score}</td>
            <td>{entry.percentOfMaxPossible !== null ? `${entry.percentOfMaxPossible}%` : '—'}</td>
            <td>{new Date(entry.date).toLocaleDateString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
