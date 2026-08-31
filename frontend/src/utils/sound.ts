let audioCtx: AudioContext | null = null

function getContext(): AudioContext | null {
  try {
    if (!audioCtx) {
      audioCtx = new AudioContext()
    }
    if (audioCtx.state === 'suspended') {
      void audioCtx.resume()
    }
    return audioCtx
  } catch {
    return null
  }
}

function playTone(freq: number, duration: number, type: OscillatorType, startOffset: number, peakGain: number) {
  const ctx = getContext()
  if (!ctx) return
  const osc = ctx.createOscillator()
  const gain = ctx.createGain()
  osc.type = type
  osc.frequency.value = freq
  const startTime = ctx.currentTime + startOffset
  gain.gain.setValueAtTime(0, startTime)
  gain.gain.linearRampToValueAtTime(peakGain, startTime + 0.01)
  gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration)
  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.start(startTime)
  osc.stop(startTime + duration + 0.05)
}

/** Your own guess was a valid, scoring word. */
export function playValidGuessSound() {
  playTone(1568, 0.35, 'sine', 0, 0.25)
  playTone(2093, 0.3, 'sine', 0.05, 0.15)
}

/** Your own guess was rejected (too short, not a word, already found, etc). */
export function playInvalidGuessSound() {
  playTone(147, 0.25, 'sawtooth', 0, 0.18)
  playTone(155, 0.25, 'square', 0, 0.1)
}

/** Another player just scored. */
export function playOpponentScoreSound() {
  playTone(660, 0.4, 'sine', 0, 0.2)
  playTone(880, 0.3, 'sine', 0.05, 0.1)
}
