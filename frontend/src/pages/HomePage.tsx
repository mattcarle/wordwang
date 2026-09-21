import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createGame, getJoinableGames, joinGame, startGame } from '../api/games'
import { createDailyGame, getDailyInfo } from '../api/daily'
import { NameEntryForm } from '../components/shared/NameEntryForm'
import { getSavedName, saveName } from '../utils/cookies'
import { getDailyCompletion } from '../utils/dailyCompletion'
import { getOrCreateDailyPlayerId } from '../utils/dailyPlayerIdentity'
import { storePlayerId } from '../utils/playerIdentity'
import wordwangPhoto from '../assets/wordwang.jpg'
import '../styles/tiles.css'
import type { JoinableGameView } from '../types/game'

type Mode = 'none' | 'new' | 'join' | 'daily'
type DailyStatus = 'loading' | 'available' | 'completed'

const WORDWANG_ROWS = ['WORD', 'WANG']

export function HomePage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<Mode>('none')
  const [joinCode, setJoinCode] = useState('')
  const [joinName, setJoinName] = useState(getSavedName())
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [joinableGames, setJoinableGames] = useState<JoinableGameView[] | null>(null)
  const [dailyStatus, setDailyStatus] = useState<DailyStatus>('loading')

  useEffect(() => {
    getDailyInfo()
      .then((info) => {
        const completion = getDailyCompletion()
        setDailyStatus(completion?.date === info.date ? 'completed' : 'available')
      })
      .catch(() => setDailyStatus('available'))
  }, [])

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

  async function handlePlayDaily(name: string) {
    setBusy(true)
    setError(null)
    try {
      saveName(name)
      const game = await createDailyGame(name, getOrCreateDailyPlayerId())
      storePlayerId(game.gameId, game.organiserId)
      await startGame(game.gameId, game.organiserId)
      navigate(`/game/${game.gameId}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start the Daily Wang')
      setBusy(false)
    }
  }

  function handleDailyClick() {
    if (dailyStatus === 'completed') {
      navigate('/daily-result')
      return
    }
    const savedName = getSavedName()
    if (savedName) {
      handlePlayDaily(savedName)
    } else {
      setMode('daily')
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
      <div className="home-wordmark" aria-label="Wordwang" role="img">
        {WORDWANG_ROWS.map((row) => (
          <div className="tile-row" key={row}>
            {row.split('').map((letter, i) => (
              <span key={i} className="tile tile-display">
                {letter}
              </span>
            ))}
          </div>
        ))}
      </div>
      <p className="tagline">Two minutes to find as many words as you can.</p>

      {mode === 'none' && (
        <div className="home-actions">
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleDailyClick}
            disabled={dailyStatus === 'loading'}
          >
            {dailyStatus === 'completed' ? 'Daily Wang ✓' : 'Daily Wang'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => setMode('new')}>
            New Game
          </button>
          <button type="button" className="btn btn-secondary" onClick={handleShowJoin}>
            Join Game
          </button>
          <div className="home-links-row">
            <Link to="/highscores" className="link">
              Leaderboard
            </Link>
            <span className="home-links-sep">|</span>
            <Link to="/how-to-play" className="link">
              How to Play
            </Link>
            <span className="home-links-sep">|</span>
            <Link to="/admin" className="link">
              Admin
            </Link>
          </div>
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

      {mode === 'daily' && (
        <NameEntryForm
          initialName={getSavedName()}
          label="Your name"
          buttonLabel="Play Daily Wang"
          onSubmit={handlePlayDaily}
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
          ← Back to Home
        </button>
      )}
    </main>
  )
}
