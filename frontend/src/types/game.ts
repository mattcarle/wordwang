export type GameStatus = 'LOBBY' | 'IN_PROGRESS' | 'FINISHED'

export type GuessOutcome = 'VALID' | 'TOO_SHORT' | 'NOT_A_WORD' | 'INVALID_LETTERS' | 'ALREADY_FOUND'

export interface PlayerView {
  playerId: string
  name: string
  score: number
}

export interface CreateGameResponse {
  gameId: string
  organiserId: string
  organiserName: string
  status: GameStatus
}

export interface JoinGameResponse {
  gameId: string
  playerId: string
  status: GameStatus
  players: PlayerView[]
}

export interface StartGameResponse {
  gameId: string
  status: GameStatus
  scrambledWord: string
  endsAt: string
}

export interface GameSummaryResponse {
  gameId: string
  organiserName: string
  status: GameStatus
  players: PlayerView[]
}

export interface GameSnapshotResponse {
  gameId: string
  status: GameStatus
  organiserId: string
  scrambledWord: string | null
  solutionWord: string | null
  endsAt: string | null
  players: PlayerView[]
  yourFoundWords: string[]
  winners: PlayerView[]
}

export interface JoinableGameView {
  gameId: string
  organiserName: string
  playerCount: number
}

export interface HighScoreView {
  playerName: string
  score: number
  playedAt: string
}

export interface PlayerJoinedEvent {
  type: 'PLAYER_JOINED'
  players: PlayerView[]
}

export interface GameStartedEvent {
  type: 'GAME_STARTED'
  scrambledWord: string
  endsAt: string
}

export interface ScoreUpdateEvent {
  type: 'SCORE_UPDATE'
  players: PlayerView[]
}

export interface GuessFeedbackEvent {
  type: 'GUESS_FEEDBACK'
  playerId: string
  word: string
  outcome: GuessOutcome
  points: number
  message: string
}

export interface GameEndedEvent {
  type: 'GAME_ENDED'
  solutionWord: string
  winners: PlayerView[]
  players: PlayerView[]
}

export type GameEvent =
  | PlayerJoinedEvent
  | GameStartedEvent
  | ScoreUpdateEvent
  | GuessFeedbackEvent
  | GameEndedEvent
