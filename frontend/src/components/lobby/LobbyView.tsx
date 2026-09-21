import { useState } from 'react'
import { Scoreboard } from '../game/Scoreboard'
import { NameEntryForm } from '../shared/NameEntryForm'
import { copyToClipboard } from '../../utils/clipboard'
import type { PlayerView } from '../../types/game'

interface LobbyViewProps {
  gameId: string
  organiserName: string
  organiserId: string
  players: PlayerView[]
  meId: string | null
  onJoin: (name: string) => void
  onStart: () => void
  joinBusy?: boolean
  joinError?: string | null
  startBusy?: boolean
  startError?: string | null
  countdownStep?: 3 | 2 | 1 | null
  isDaily?: boolean
}

export function LobbyView({
  gameId,
  organiserName,
  organiserId,
  players,
  meId,
  onJoin,
  onStart,
  joinBusy,
  joinError,
  startBusy,
  startError,
  countdownStep,
  isDaily,
}: LobbyViewProps) {
  const [copyState, setCopyState] = useState<'idle' | 'copied' | 'failed'>('idle')
  const inviteUrl = `${window.location.origin}${import.meta.env.BASE_URL.replace(/\/$/, '')}/game/${gameId}`
  const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(
    `Join my WordWang game! Code: ${gameId}\n${inviteUrl}`,
  )}`

  async function copyInviteLink() {
    const success = await copyToClipboard(inviteUrl)
    setCopyState(success ? 'copied' : 'failed')
    if (success) {
      setTimeout(() => setCopyState('idle'), 2000)
    }
  }

  const isOrganiser = meId === organiserId
  const isStarting = countdownStep != null

  // The Daily Wang game is solo and starts itself automatically - there's no room to invite
  // anyone to and no reason to wait on a "Start Game" click, so skip straight to a plain
  // "get ready" screen instead of the normal multiplayer lobby.
  if (isDaily) {
    return (
      <div className="lobby-view lobby-view-daily">
        <h1>Daily Wang</h1>
        <p className="waiting-message">Get ready…</p>
        {countdownStep != null && (
          <div className="countdown-overlay" role="status" aria-live="assertive">
            <span className="countdown-number" key={countdownStep}>
              {countdownStep}
            </span>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="lobby-view">
      <h1>{organiserName}'s Game</h1>
      <p className="game-code">
        Game code: <strong>{gameId}</strong>
      </p>

      <div className="lobby-players">
        <h2>Players</h2>
        <Scoreboard players={players} meId={meId} />
      </div>

      {meId === null && isStarting && <p className="waiting-message">The game is starting…</p>}

      {meId === null && !isStarting && (
        <NameEntryForm
          label="Your name"
          buttonLabel="Join Game"
          onSubmit={onJoin}
          busy={joinBusy}
          error={joinError}
        />
      )}

      {meId !== null && isOrganiser && (
        <div className="lobby-organiser-actions">
          <div className="invite-box">
            <button type="button" className="btn btn-secondary" onClick={copyInviteLink}>
              {copyState === 'copied' ? 'Copied!' : copyState === 'failed' ? "Couldn't copy" : 'Copy Invite Link'}
            </button>
            <a className="btn btn-secondary" href={whatsappUrl} target="_blank" rel="noreferrer">
              Share via WhatsApp
            </a>
          </div>
          {copyState === 'failed' && (
            <p className="form-error">
              Couldn't copy automatically — copy this link manually: <span className="invite-url">{inviteUrl}</span>
            </p>
          )}
          {startError && <p className="form-error">{startError}</p>}
          <button type="button" className="btn btn-primary" onClick={onStart} disabled={startBusy || isStarting}>
            {startBusy || isStarting ? 'Starting…' : 'Start Game'}
          </button>
        </div>
      )}

      {meId !== null && !isOrganiser && <p className="waiting-message">Waiting for the game to start…</p>}

      {countdownStep != null && (
        <div className="countdown-overlay" role="status" aria-live="assertive">
          <span className="countdown-number" key={countdownStep}>
            {countdownStep}
          </span>
        </div>
      )}
    </div>
  )
}
