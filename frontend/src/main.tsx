import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'

// This app is served from a path prefix (e.g. /wordwang/) behind the shared carle7-edge reverse
// proxy, not the domain root (see vite.config.ts's `base`) - the router needs to know that prefix
// too, or it tries to match routes against the full "/wordwang/..." pathname and finds nothing.
const basename = import.meta.env.BASE_URL.replace(/\/$/, '')

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter basename={basename}>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
