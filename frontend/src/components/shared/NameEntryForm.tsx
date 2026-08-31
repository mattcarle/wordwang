import { useState, type FormEvent } from 'react'

interface NameEntryFormProps {
  initialName?: string
  label: string
  buttonLabel: string
  onSubmit: (name: string) => void
  busy?: boolean
  error?: string | null
}

export function NameEntryForm({ initialName, label, buttonLabel, onSubmit, busy, error }: NameEntryFormProps) {
  const [name, setName] = useState(initialName ?? '')

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const trimmed = name.trim()
    if (!trimmed) return
    onSubmit(trimmed)
  }

  return (
    <form className="name-entry-form" onSubmit={handleSubmit}>
      <label htmlFor="player-name">{label}</label>
      <input
        id="player-name"
        type="text"
        value={name}
        onChange={(e) => setName(e.target.value)}
        maxLength={10}
        autoFocus
        required
      />
      {error && <p className="form-error">{error}</p>}
      <button type="submit" className="btn btn-primary" disabled={busy || !name.trim()}>
        {busy ? 'Please wait…' : buttonLabel}
      </button>
    </form>
  )
}
