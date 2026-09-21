export interface AuditPlayerView {
  name: string
  organiser: boolean
  score: number
  foundEightLetterWord: boolean
  winner: boolean
  ipAddress: string | null
  location: string
  foundWords: string[]
}

export interface AuditGameView {
  gameCode: string
  createdAt: string
  solutionWord: string
  startedAt: string | null
  finishedAt: string | null
  maxPossibleScore: number | null
  dailyChallengeDate: string | null
  endedByQuit: boolean | null
  playerCount: number
  players: AuditPlayerView[]
}

export interface AuditPageResponse {
  games: AuditGameView[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
