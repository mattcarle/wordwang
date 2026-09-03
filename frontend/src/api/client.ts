export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

// Derived from Vite's `base` (see vite.config.ts) rather than hardcoded, since this app is served
// from a path prefix (e.g. /wordwang/) behind the shared carle7-edge reverse proxy, not the
// domain root - API calls are namespaced under that same prefix. Caddy strips it back off before
// the request reaches the backend (see frontend/Caddyfile).
const API_BASE = import.meta.env.BASE_URL.replace(/\/$/, '')

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'))
  return match ? decodeURIComponent(match[1]) : null
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const method = (init?.method ?? 'GET').toUpperCase()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init?.headers as Record<string, string> | undefined),
  }

  // Only the admin endpoints (see api/admin.ts) sit behind Spring Security's CSRF filter -
  // gameplay stays session/CSRF-free - but attaching this on every state-changing request is
  // harmless when the cookie doesn't exist, so it's simplest to do it here for every caller.
  if (method !== 'GET' && method !== 'HEAD') {
    const csrfToken = getCookie('XSRF-TOKEN')
    if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
  })

  if (!response.ok) {
    let message = response.statusText
    try {
      const body = await response.json()
      if (body?.error) {
        message = body.error
      }
    } catch {
      // response body wasn't JSON, fall back to statusText
    }
    throw new ApiError(response.status, message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export function get<T>(path: string): Promise<T> {
  return request<T>(path)
}

export function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

export function del<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' })
}
