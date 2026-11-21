import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createGame, joinGame, startGame } from '../api/games'
import { useJoinableGames } from '../hooks/useJoinableGames'
import { createDailyGame, getDailyInfo } from '../api/daily'
import { NameEntryForm } from '../components/shared/NameEntryForm'
import { getSavedName, saveName } from '../utils/cookies'
import { getDailyCompletion } from '../utils/dailyCompletion'
import { getOrCreateDailyPlayerId } from '../utils/dailyPlayerIdentity'
import { storePlayerId } from '../utils/playerIdentity'
import wordwangPhoto from '../assets/wordwang.jpg'
import '../styles/tiles.css'

type Mode = 'none' | 'daily' | 'single' | 'multi'
type DailyStatus = 'loading' | 'available' | 'completed'

/** Sentinel for the "New Game" radio option; real game ids are 5-digit codes so it can't collide. */
const NEW_GAME = 'new'

const WORDWANG_ROWS = ['WORD', 'WANG']

export function HomePage() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<Mode>('none')
  const [moreWang, setMoreWang] = useState(false)
  const [selectedGameId, setSelectedGameId] = useState<string | null>(null)
  const [joinName, setJoinName] = useState(getSavedName())
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const joinableGames = useJoinableGames(mode === 'multi')
  const [dailyStatus, setDailyStatus] = useState<DailyStatus>('loading')

  // Keep the radio selection valid as the live list changes: pick a default once the first list
  // arrives, and fall back if the selected game has since started or been closed.
  useEffect(() => {
    if (joinableGames === null) return
    setSelectedGameId((current) => {
      if (current === NEW_GAME) return current
      if (current !== null && joinableGames.some((g) => g.gameId === current)) return current
      return joinableGames[0]?.gameId ?? NEW_GAME
    })
  }, [joinableGames])

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

  async function handlePlaySingle(name: string) {
    setBusy(true)
    setError(null)
    try {
      saveName(name)
      const game = await createGame(name, true)
      storePlayerId(game.gameId, game.organiserId)
      await startGame(game.gameId, game.organiserId)
      navigate(`/game/${game.gameId}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start the game')
      setBusy(false)
    }
  }

  function handleSingleClick() {
    const savedName = getSavedName()
    if (savedName) {
      handlePlaySingle(savedName)
    } else {
      setMode('single')
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

  function handleShowMulti() {
    setSelectedGameId(null)
    setMode('multi')
  }

  async function handleJoinGame(e: FormEvent) {
    e.preventDefault()
    const name = joinName.trim()
    if (!selectedGameId || !name) {
      setError('Select a game and enter your name')
      return
    }
    if (selectedGameId === NEW_GAME) {
      handleNewGame(name)
      return
    }
    setBusy(true)
    setError(null)
    try {
      saveName(name)
      const joined = await joinGame(selectedGameId, name)
      storePlayerId(selectedGameId, joined.playerId)
      navigate(`/game/${selectedGameId}`)
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
            disabled={dailyStatus !== 'available'}
          >
            {dailyStatus === 'completed' ? 'Daily Wang ✓' : 'Daily Wang'}
          </button>
          <label className="leaderboard-option more-wang-option">
            <input type="checkbox" checked={moreWang} onChange={(e) => setMoreWang(e.target.checked)} />
            More Wang?
          </label>
          {moreWang && (
            <>
              <button type="button" className="btn btn-secondary" onClick={handleSingleClick}>
                Single Player
              </button>
              <button type="button" className="btn btn-secondary" onClick={handleShowMulti}>
                Multiplayer
              </button>
            </>
          )}
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

      {mode === 'multi' && (
        <form className="name-entry-form" onSubmit={handleJoinGame}>
          {joinableGames === null && <p className="tagline">Looking for live games…</p>}

          {joinableGames !== null && (
            <div className="joinable-games">
              {joinableGames.length > 0 && <h2>Live games</h2>}
              <ul>
                {joinableGames.map((game) => (
                  <li key={game.gameId}>
                    <label className="leaderboard-option">
                      <input
                        type="radio"
                        name="joinable-game"
                        checked={selectedGameId === game.gameId}
                        onChange={() => setSelectedGameId(game.gameId)}
                      />
                      {game.name} — {game.playerCount}{' '}
                      {game.playerCount === 1 ? 'player' : 'players'} waiting
                    </label>
                  </li>
                ))}
                <li>
                  <label className="leaderboard-option">
                    <input
                      type="radio"
                      name="joinable-game"
                      checked={selectedGameId === NEW_GAME}
                      onChange={() => setSelectedGameId(NEW_GAME)}
                    />
                    New Game
                  </label>
                </li>
              </ul>
            </div>
          )}

          <label htmlFor="player-name">Your name</label>
          <input
            id="player-name"
            type="text"
            value={joinName}
            onChange={(e) => setJoinName(e.target.value)}
            maxLength={10}
            required
          />
          {error && <p className="form-error">{error}</p>}

          {joinableGames !== null && (
            <button type="submit" className="btn btn-primary" disabled={busy || !joinName.trim() || !selectedGameId}>
              {busy ? 'Please wait…' : selectedGameId === NEW_GAME ? 'Create Game' : 'Join Game'}
            </button>
          )}
        </form>
      )}

      {mode === 'single' && (
        <NameEntryForm
          initialName={getSavedName()}
          label="Your name"
          buttonLabel="Start Game"
          onSubmit={handlePlaySingle}
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

      {mode !== 'none' && (
        <button type="button" className="link back-link" onClick={() => {
            setError(null)
            setMode('none')
          }}>
          ← Back to Home
        </button>
      )}
    </main>
  )
}
