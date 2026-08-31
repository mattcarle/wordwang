import type { PlayerView } from '../../types/game'

interface ScoreboardProps {
  players: PlayerView[]
  meId?: string | null
}

export function Scoreboard({ players, meId }: ScoreboardProps) {
  const sorted = [...players].sort((a, b) => b.score - a.score)

  return (
    <ol className="scoreboard">
      {sorted.map((player) => (
        <li key={player.playerId} className={player.playerId === meId ? 'scoreboard-me' : undefined}>
          <span className="scoreboard-name">{player.name}</span>
          <span className="scoreboard-score">{player.score}</span>
        </li>
      ))}
    </ol>
  )
}
