import { useEffect, useRef, useState, type CSSProperties } from 'react'
import '../../styles/tiles.css'

interface TileInputProps {
  letters: string
  onSubmit: (word: string) => void
  disabled?: boolean
}

const SHUFFLE_ANIMATION_MS = 380

function shuffledCopy<T>(items: T[]): T[] {
  const copy = [...items]
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[copy[i], copy[j]] = [copy[j], copy[i]]
  }
  return copy
}

interface ShuffleVars {
  dx: number
  dy: number
  rot: number
}

export function TileInput({ letters, onSubmit, disabled }: TileInputProps) {
  const tiles = letters.split('')
  const [usedIndices, setUsedIndices] = useState<number[]>([])
  const [order, setOrder] = useState<number[]>(() => tiles.map((_, i) => i))
  const [shuffleAnim, setShuffleAnim] = useState<Record<number, ShuffleVars> | null>(null)
  const [isShuffling, setIsShuffling] = useState(false)
  const bankTileRefs = useRef<Record<number, HTMLButtonElement | null>>({})
  const shuffleTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    setOrder(tiles.map((_, i) => i))
    // Only the letter set identifies a new round - re-deriving `tiles` every render isn't wanted here.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [letters])

  // Cancel a pending shuffle-animation cleanup on unmount so it can't fire setState afterwards.
  useEffect(() => {
    return () => {
      if (shuffleTimeoutRef.current) clearTimeout(shuffleTimeoutRef.current)
    }
  }, [])

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

  function shuffle() {
    if (disabled || isShuffling || order.length < 2) return

    const before: Record<number, DOMRect> = {}
    order.forEach((tileIndex) => {
      const el = bankTileRefs.current[tileIndex]
      if (el) before[tileIndex] = el.getBoundingClientRect()
    })

    let next = order
    for (let attempt = 0; attempt < 5 && next.join(',') === order.join(','); attempt++) {
      next = shuffledCopy(order)
    }

    setIsShuffling(true)
    setOrder(next)

    requestAnimationFrame(() => {
      const vars: Record<number, ShuffleVars> = {}
      next.forEach((tileIndex) => {
        const el = bankTileRefs.current[tileIndex]
        const from = before[tileIndex]
        if (!el || !from) return
        const to = el.getBoundingClientRect()
        const dx = from.left - to.left
        const dy = from.top - to.top
        const rot = Math.max(-14, Math.min(14, -dx / 6))
        vars[tileIndex] = { dx, dy, rot }
      })
      setShuffleAnim(vars)
      shuffleTimeoutRef.current = setTimeout(() => {
        setShuffleAnim(null)
        setIsShuffling(false)
      }, SHUFFLE_ANIMATION_MS)
    })
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
      <div className="tile-current-row-wrap">
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

        {currentWord.length > 0 && (
          <button
            type="button"
            className="btn btn-secondary tile-backspace"
            onClick={removeLast}
            disabled={disabled}
            aria-label="Remove last letter"
          >
            ⌫
          </button>
        )}
      </div>

      <div className="tile-bank-row">
        <div className="tile-row tile-row-bank" aria-label="Available letters">
          {order.map((tileIndex) => {
            const anim = shuffleAnim?.[tileIndex]
            return (
              <button
                key={tileIndex}
                ref={(el) => {
                  bankTileRefs.current[tileIndex] = el
                }}
                type="button"
                className={`tile${anim ? ' tile-shuffling' : ''}`}
                disabled={disabled || usedIndices.includes(tileIndex)}
                onClick={() => placeTile(tileIndex)}
                style={
                  anim
                    ? ({
                        '--shuffle-dx': `${anim.dx}px`,
                        '--shuffle-dy': `${anim.dy}px`,
                        '--shuffle-rot': `${anim.rot}deg`,
                      } as CSSProperties)
                    : undefined
                }
              >
                {tiles[tileIndex]}
              </button>
            )
          })}
        </div>

        <button
          type="button"
          className="tile-shuffle-btn"
          onClick={shuffle}
          disabled={disabled || isShuffling}
          aria-label="Shuffle letters"
          title="Shuffle letters"
        >
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
            <path
              d="M3 6h3.5c1.4 0 2.7.7 3.5 1.9l6 8.6c.8 1.2 2.1 1.9 3.5 1.9H21M17 15l4 3-4 3M3 18h3.5c1.4 0 2.7-.7 3.5-1.9l.6-.9M17 6l4 3-4 3M14.4 8.9l.2-.3c.8-1.2 2.1-1.9 3.5-1.9H21"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      </div>

      <div className="tile-bottom-actions">
        <button type="button" className="btn btn-secondary tile-action-btn" onClick={clear} disabled={disabled || currentWord.length === 0}>
          Clear
        </button>
        <button type="button" className="btn btn-primary tile-action-btn" onClick={submit} disabled={disabled || currentWord.length === 0}>
          Wang!
        </button>
      </div>
    </div>
  )
}
