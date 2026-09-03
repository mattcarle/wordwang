import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { clearHighScores, getMe, getSetupStatus, login, logout, setupAdmin } from '../api/admin'

type AuthState = 'loading' | 'setup' | 'login' | 'authenticated'

export function AdminPage() {
  const navigate = useNavigate()
  const [authState, setAuthState] = useState<AuthState>('loading')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [clearStatus, setClearStatus] = useState<string | null>(null)

  useEffect(() => {
    async function checkAuth() {
      try {
        await getMe()
        setAuthState('authenticated')
      } catch {
        try {
          const status = await getSetupStatus()
          setAuthState(status.setupRequired ? 'setup' : 'login')
        } catch (err) {
          setError(err instanceof Error ? err.message : 'Failed to load admin page')
        }
      }
    }
    checkAuth()
  }, [])

  async function handleSetup(e: FormEvent) {
    e.preventDefault()
    setError(null)
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setBusy(true)
    try {
      await setupAdmin(password)
      setAuthState('authenticated')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set password')
    } finally {
      setBusy(false)
    }
  }

  async function handleLogin(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      await login(password)
      setAuthState('authenticated')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Incorrect password')
    } finally {
      setBusy(false)
    }
  }

  async function handleLogout() {
    setBusy(true)
    try {
      await logout()
    } finally {
      setBusy(false)
      setPassword('')
      setAuthState('login')
    }
  }

  async function handleClearHighScores() {
    if (!window.confirm('Are you sure you want to clear the high score table? This cannot be undone.')) {
      return
    }
    setBusy(true)
    setClearStatus(null)
    try {
      await clearHighScores()
      setClearStatus('High score table cleared.')
    } catch (err) {
      setClearStatus(err instanceof Error ? err.message : 'Failed to clear high scores')
    } finally {
      setBusy(false)
    }
  }

  if (authState === 'loading') {
    return (
      <main className="page">
        <p>Loading…</p>
      </main>
    )
  }

  if (authState === 'setup') {
    return (
      <main className="page">
        <h1>Set Admin Password</h1>
        <p>No admin password has been set yet. Choose one to secure this page.</p>
        <form className="name-entry-form" onSubmit={handleSetup}>
          <label htmlFor="admin-password">Password</label>
          <input
            id="admin-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={8}
            autoFocus
            required
          />
          <label htmlFor="admin-password-confirm">Confirm password</label>
          <input
            id="admin-password-confirm"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            minLength={8}
            required
          />
          {error && <p className="form-error">{error}</p>}
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Please wait…' : 'Set Password'}
          </button>
        </form>
      </main>
    )
  }

  if (authState === 'login') {
    return (
      <main className="page">
        <h1>Admin Login</h1>
        <form className="name-entry-form" onSubmit={handleLogin}>
          <label htmlFor="admin-password">Password</label>
          <input
            id="admin-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoFocus
            required
          />
          {error && <p className="form-error">{error}</p>}
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Please wait…' : 'Log In'}
          </button>
        </form>
        <Link to="/" className="link admin-home-link">
          ← Back to Home
        </Link>
      </main>
    )
  }

  return (
    <main className="page">
      <h1>Admin</h1>
      <button type="button" className="btn btn-secondary" onClick={handleClearHighScores} disabled={busy}>
        Clear High Score Table
      </button>
      {clearStatus && <p className="form-success">{clearStatus}</p>}

      <button
        type="button"
        className="btn btn-secondary admin-audit-link"
        onClick={() => navigate('/admin/audit')}
      >
        View Audit Trail
      </button>

      <button type="button" className="btn btn-secondary admin-logout" onClick={handleLogout} disabled={busy}>
        Log Out
      </button>

      <Link to="/" className="link admin-home-link">
        ← Back to Home
      </Link>
    </main>
  )
}
