import { Route, Routes } from 'react-router-dom'
import './App.css'
import { HomePage } from './pages/HomePage'
import { HowToPlayPage } from './pages/HowToPlayPage'
import { GamePage } from './pages/GamePage'
import { HighScoresPage } from './pages/HighScoresPage'

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/how-to-play" element={<HowToPlayPage />} />
      <Route path="/game/:gameId" element={<GamePage />} />
      <Route path="/highscores" element={<HighScoresPage />} />
    </Routes>
  )
}

export default App
