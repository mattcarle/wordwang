import { Link } from 'react-router-dom'

export function HowToPlayPage() {
  return (
    <main className="page">
      <h1>How to Play</h1>
      <ol className="how-to-play-list">
        <li>An 8-letter word is scrambled and shown to everyone as tiles.</li>
        <li>
          You have <strong>3 minutes</strong> to find as many words as you can using those letters — including,
          if you can spot it, the full 8-letter word.
        </li>
        <li>Click tiles or type on your keyboard. You can only use each letter as many times as it appears.</li>
        <li>Longer words score more points:</li>
      </ol>
      <table className="scoring-table">
        <thead>
          <tr>
            <th>Word length</th>
            <th>Points</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>1–2 letters</td>
            <td>0</td>
          </tr>
          <tr>
            <td>3 letters</td>
            <td>1</td>
          </tr>
          <tr>
            <td>4 letters</td>
            <td>3</td>
          </tr>
          <tr>
            <td>5 letters</td>
            <td>7</td>
          </tr>
          <tr>
            <td>6 letters</td>
            <td>10</td>
          </tr>
          <tr>
            <td>7 letters</td>
            <td>15</td>
          </tr>
          <tr>
            <td>8 letters</td>
            <td>20</td>
          </tr>
        </tbody>
      </table>
      <p>
        Play alone against the clock, or start a game and invite friends — everyone sees live scores as they play.
        Whoever has the highest score when the timer runs out wins!
      </p>
      <Link to="/" className="link">
        ← Back to home
      </Link>
    </main>
  )
}
