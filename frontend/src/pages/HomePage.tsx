import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createGame, getJoinableGames, joinGame } from '../api/games'
import { NameEntryForm } from '../components/shared/NameEntryForm'
import { getSavedName, saveName } from '../utils/cookies'
import { storePlayerId } from '../utils/playerIdentity'
import wordwangPhoto from '../assets/wordwang.jpg'
import type { JoinableGameView } from '../types/game'

type Mode = 'none' | 'new' | 'join'

export function HomePage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<Mode>('none')
  const [joinCode, setJoinCode] = useState('')
  const [joinName, setJoinName] = useState(getSavedName())
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [joinableGames, setJoinableGames] = useState<JoinableGameView[] | null>(null)

  async function handleNewGame(name: string) {
    setBusy(true)
    setError(null)
    try {
      saveName(name)
      const game = await createGame(name)
      storePlayerId(game.gameId, game.organiserId)
      navigate(`/game/${game.gameId}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create game')
      setBusy(false)
    }
  }

  async function handleShowJoin() {
    setMode('join')
    setJoinableGames(null)
    try {
      const games = await getJoinableGames()
      setJoinableGames(games)
    } catch {
      setJoinableGames([])
    }
  }

  async function handleJoinGame(e: FormEvent) {
    e.preventDefault()
    const code = joinCode.trim()
    const name = joinName.trim()
    if (!/^\d{5}$/.test(code) || !name) {
      setError('Enter the 5-digit game code and your name')
      return
    }
    setBusy(true)
    setError(null)
    try {
      saveName(name)
      const joined = await joinGame(code, name)
      storePlayerId(code, joined.playerId)
      navigate(`/game/${code}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to join game')
      setBusy(false)
    }
  }

  return (
    <main className="home">
      <img src={wordwangPhoto} alt="" className="home-photo" />
      <h1>Let's Play Wordwang!</h1>
      <p className="tagline">Two minutes to find as many words as you can.</p>

      {mode === 'none' && (
        <div className="home-actions">
          <button type="button" className="btn btn-primary" onClick={() => setMode('new')}>
            New Game
          </button>
          <button type="button" className="btn btn-secondary" onClick={handleShowJoin}>
            Join Game
          </button>
          <Link to="/how-to-play" className="link">
            How to Play
          </Link>
        </div>
      )}

      {mode === 'new' && (
        <NameEntryForm
          initialName={getSavedName()}
          label="Your name"
          buttonLabel="Create Game"
          onSubmit={handleNewGame}
          busy={busy}
          error={error}
        />
      )}

      {mode === 'join' && (
        <>
          {joinableGames === null && <p className="tagline">Looking for live games…</p>}

          {joinableGames !== null && joinableGames.length > 0 && (
            <div className="joinable-games">
              <h2>Live games</h2>
              <ul>
                {joinableGames.map((game) => (
                  <li key={game.gameId}>
                    <Link to={`/game/${game.gameId}`} className="link">
                      {game.organiserName}'s game — {game.playerCount}{' '}
                      {game.playerCount === 1 ? 'player' : 'players'} waiting
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {joinableGames !== null && joinableGames.length === 0 && (
            <p className="tagline">No live games right now — enter a code below.</p>
          )}

          <form className="name-entry-form" onSubmit={handleJoinGame}>
            <label htmlFor="join-code">Game code</label>
            <input
              id="join-code"
              type="text"
              inputMode="numeric"
              pattern="\d{5}"
              maxLength={5}
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value.replace(/\D/g, ''))}
              autoFocus
              required
            />
            <label htmlFor="join-name">Your name</label>
            <input
              id="join-name"
              type="text"
              value={joinName}
              onChange={(e) => setJoinName(e.target.value)}
              maxLength={10}
              required
            />
            {error && <p className="form-error">{error}</p>}
            <button type="submit" className="btn btn-primary" disabled={busy}>
              {busy ? 'Please wait…' : 'Join Game'}
            </button>
          </form>
        </>
      )}

      {mode !== 'none' && (
        <button type="button" className="link back-link" onClick={() => setMode('none')}>
          ← Back
        </button>
      )}
    </main>
  )
}
