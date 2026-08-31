import { useEffect, useState } from 'react'

interface CountdownTimerProps {
  endsAt: string
}

export function CountdownTimer({ endsAt }: CountdownTimerProps) {
  const [remainingMs, setRemainingMs] = useState(() => new Date(endsAt).getTime() - Date.now())

  useEffect(() => {
    const interval = setInterval(() => {
      setRemainingMs(new Date(endsAt).getTime() - Date.now())
    }, 200)
    return () => clearInterval(interval)
  }, [endsAt])

  const seconds = Math.max(0, Math.ceil(remainingMs / 1000))

  return (
    <div className={`countdown-timer${seconds <= 10 ? ' countdown-timer-urgent' : ''}`}>
      {seconds}s
    </div>
  )
}
