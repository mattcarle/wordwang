const DAILY_COOKIE = 'wordwang_daily'
const ONE_YEAR_SECONDS = 60 * 60 * 24 * 365

export interface DailyCompletion {
  date: string
  score: number
  maxPossibleScore: number
  solutionWord: string
  foundWords: string[]
}

export function getDailyCompletion(): DailyCompletion | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${DAILY_COOKIE}=([^;]*)`))
  if (!match) return null
  try {
    const parsed = JSON.parse(decodeURIComponent(match[1]))
    if (
      parsed &&
      typeof parsed.date === 'string' &&
      typeof parsed.score === 'number' &&
      typeof parsed.maxPossibleScore === 'number' &&
      typeof parsed.solutionWord === 'string' &&
      Array.isArray(parsed.foundWords)
    ) {
      return parsed as DailyCompletion
    }
    return null
  } catch {
    return null
  }
}

export function saveDailyCompletion(completion: DailyCompletion): void {
  document.cookie = `${DAILY_COOKIE}=${encodeURIComponent(JSON.stringify(completion))}; max-age=${ONE_YEAR_SECONDS}; path=/; samesite=lax`
}
