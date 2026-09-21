import { Navigate } from 'react-router-dom'
import { ResultsView } from '../components/results/ResultsView'
import { getDailyCompletion } from '../utils/dailyCompletion'
import type { PlayerView } from '../types/game'

const DAILY_RESULT_PLAYER_ID = 'daily-result'

export function DailyResultPage() {
  const completion = getDailyCompletion()

  if (!completion) {
    return <Navigate to="/" replace />
  }

  const you: PlayerView = { playerId: DAILY_RESULT_PLAYER_ID, name: 'You', score: completion.score }

  return (
    <main className="page">
      <ResultsView
        solutionWord={completion.solutionWord}
        winners={[you]}
        players={[you]}
        meId={DAILY_RESULT_PLAYER_ID}
        yourFoundWords={completion.foundWords}
        maxPossibleScore={completion.maxPossibleScore}
        dailyChallengeDate={completion.date}
      />
    </main>
  )
}
