import { useEffect, useState } from 'react'
import '../../styles/tiles.css'

interface TileInputProps {
  letters: string
  onSubmit: (word: string) => void
  disabled?: boolean
}

export function TileInput({ letters, onSubmit, disabled }: TileInputProps) {
  const tiles = letters.split('')
  const [usedIndices, setUsedIndices] = useState<number[]>([])

  const currentWord = usedIndices.map((i) => tiles[i]).join('')

  function placeTile(index: number) {
    if (disabled || usedIndices.includes(index)) return
    setUsedIndices((prev) => [...prev, index])
  }

  function removeLast() {
    setUsedIndices((prev) => prev.slice(0, -1))
  }

  function clear() {
    setUsedIndices([])
  }

  function submit() {
    if (disabled || currentWord.length === 0) return
    onSubmit(currentWord)
    clear()
  }

  useEffect(() => {
    if (disabled) return

    function handleKeyDown(e: KeyboardEvent) {
      if (e.metaKey || e.ctrlKey || e.altKey) return

      if (e.key === 'Backspace') {
        e.preventDefault()
        removeLast()
        return
      }
      if (e.key === 'Escape') {
        e.preventDefault()
        clear()
        return
      }
      if (e.key === 'Enter') {
        e.preventDefault()
        submit()
        return
      }
      if (/^[a-zA-Z]$/.test(e.key)) {
        const letter = e.key.toUpperCase()
        setUsedIndices((prev) => {
          const availableIndex = tiles.findIndex((t, i) => t === letter && !prev.includes(i))
          if (availableIndex === -1) return prev
          return [...prev, availableIndex]
        })
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [disabled, letters, currentWord])

  return (
    <div className="tile-input">
      <div className="tile-row tile-row-current" aria-label="Your word">
        {usedIndices.length === 0 && <span className="tile-placeholder">Type or click letters…</span>}
        {usedIndices.map((tileIndex, position) => (
          <button
            key={position}
            type="button"
            className="tile tile-active"
            onClick={() => setUsedIndices((prev) => prev.filter((_, i) => i !== position))}
          >
            {tiles[tileIndex]}
          </button>
        ))}
      </div>

      <div className="tile-row tile-row-bank" aria-label="Available letters">
        {tiles.map((letter, index) => (
          <button
            key={index}
            type="button"
            className="tile"
            disabled={disabled || usedIndices.includes(index)}
            onClick={() => placeTile(index)}
          >
            {letter}
          </button>
        ))}
      </div>

      <div className="tile-input-actions">
        <button
          type="button"
          className="btn btn-secondary tile-backspace"
          onClick={removeLast}
          disabled={disabled || currentWord.length === 0}
          aria-label="Remove last letter"
        >
          ⌫
        </button>
        <button type="button" className="btn btn-secondary" onClick={clear} disabled={disabled || currentWord.length === 0}>
          Clear
        </button>
        <button type="button" className="btn btn-primary" onClick={submit} disabled={disabled || currentWord.length === 0}>
          Submit
        </button>
      </div>
    </div>
  )
}
