export interface AuditPlayerView {
  name: string
  organiser: boolean
  score: number
  foundEightLetterWord: boolean
  winner: boolean
  ipAddress: string | null
  location: string
}

export interface AuditGameView {
  gameCode: string
  createdAt: string
  solutionWord: string
  players: AuditPlayerView[]
}

export interface AuditPageResponse {
  games: AuditGameView[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
