const NAME_COOKIE = 'wordwang_name'
const ONE_YEAR_SECONDS = 60 * 60 * 24 * 365

export function getSavedName(): string {
  const match = document.cookie.match(new RegExp(`(?:^|; )${NAME_COOKIE}=([^;]*)`))
  return match ? decodeURIComponent(match[1]) : ''
}

export function saveName(name: string): void {
  document.cookie = `${NAME_COOKIE}=${encodeURIComponent(name)}; max-age=${ONE_YEAR_SECONDS}; path=/; samesite=lax`
}
