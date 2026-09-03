import { Route, Routes } from 'react-router-dom'
import './App.css'
import { HomePage } from './pages/HomePage'
import { HowToPlayPage } from './pages/HowToPlayPage'
import { GamePage } from './pages/GamePage'
import { HighScoresPage } from './pages/HighScoresPage'
import { AdminPage } from './pages/AdminPage'
import { AuditTrailPage } from './pages/AuditTrailPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/how-to-play" element={<HowToPlayPage />} />
      <Route path="/game/:gameId" element={<GamePage />} />
      <Route path="/highscores" element={<HighScoresPage />} />
      <Route path="/admin" element={<AdminPage />} />
      <Route path="/admin/audit" element={<AuditTrailPage />} />
    </Routes>
  )
}

export default App
