import type { PlayerView } from '../../types/game'

interface ScoreboardProps {
  players: PlayerView[]
  meId?: string | null
  flashPlayerId?: string | null
  flashToken?: number
}

export function Scoreboard({ players, meId, flashPlayerId, flashToken }: ScoreboardProps) {
  const sorted = [...players].sort((a, b) => b.score - a.score)

  return (
    <ol className="scoreboard">
      {sorted.map((player) => {
        const isFlashing = player.playerId === flashPlayerId
        const className = [player.playerId === meId ? 'scoreboard-me' : '', isFlashing ? 'scoreboard-flash' : '']
          .filter(Boolean)
          .join(' ')
        return (
          <li
            key={isFlashing ? `${player.playerId}-flash-${flashToken}` : player.playerId}
            className={className || undefined}
          >
            <span className="scoreboard-name">{player.name}</span>
            <span className="scoreboard-score">{player.score}</span>
          </li>
        )
      })}
    </ol>
  )
}
