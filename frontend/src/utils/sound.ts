import wordwangJingleUrl from '../assets/thats-wordwang.m4a'
import letsPlayJingleUrl from '../assets/lets-play-wordwang2.m4a'
// Field recording of concert-hall applause (De Doelen, Rotterdam) by Sandermotions on Freesound,
// mirrored on Wikimedia Commons - CC0 / public domain, no attribution required.
// https://commons.wikimedia.org/wiki/File:277021_sandermotions_applause-2.wav
import applauseUrl from '../assets/applause.wav'

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

/** You found the full 8-letter word. */
export function playWordwangJingle() {
  try {
    void new Audio(wordwangJingleUrl).play()
  } catch {
    // ignore playback failures (e.g. autoplay restrictions)
  }
}

/**
 * Nominal length of the "Let's play WordWang!" clip. Used to pace the countdown that follows it -
 * deliberately not measured via the audio element's own `ended` event, since fetch/decode latency
 * can make that fire well after the clip has actually finished playing.
 */
export const START_JINGLE_DURATION_MS = 1000

/** The organiser just hit Start Game. */
export function playStartGameJingle() {
  try {
    void new Audio(letsPlayJingleUrl).play()
  } catch {
    // ignore playback failures (e.g. autoplay restrictions)
  }
}

/** One tick ("3", "2", "1") of the pre-game countdown. */
export function playCountdownTickSound() {
  playTone(880, 0.18, 'sine', 0, 0.22)
}

/** The countdown reaching "Go!" as the game begins. */
export function playCountdownGoSound() {
  playTone(1046, 0.2, 'sine', 0, 0.25)
  playTone(1568, 0.35, 'sine', 0.1, 0.2)
}

/** The round has ended - either the timer ran out or the organiser quit early. */
export function playGameEndSound() {
  playTone(880, 0.25, 'sine', 0, 0.22)
  playTone(660, 0.25, 'sine', 0.18, 0.2)
  playTone(440, 0.45, 'sine', 0.36, 0.2)
}

/** You won the game. */
export function playApplauseSound() {
  try {
    void new Audio(applauseUrl).play()
  } catch {
    // ignore playback failures (e.g. autoplay restrictions)
  }
}
