/**
 * Soulful Romantic Acoustic Piano & Celesta Melody Synthesizer
 * Plays an emotional, tender, acoustic romantic piano melody in the background.
 * Inspired by classic romantic movie themes (rich chords, deep warm bass, expressive melody).
 */

class RomanticPianoEngine {
  constructor() {
    this.ctx = null;
    this.isPlaying = false;
    this.timer = null;
    this.currentStep = 0;
    this.volume = 0.65;
    this.masterGain = null;
    this.reverbNode = null;
    this.audioElement = null;

    // Musical Frequencies (Hz)
    // C2=65.41, D2=73.42, E2=82.41, F2=87.31, G2=98.00, A2=110.00, B2=123.47
    // C3=130.81, D3=146.83, E3=164.81, F3=174.61, G3=196.00, A3=220.00, B3=246.94
    // C4=261.63, D4=293.66, E4=329.63, F4=349.23, G4=392.00, A4=440.00, B4=493.88
    // C5=523.25, D5=587.33, E5=659.25, F5=698.46, G5=783.99, A5=880.00, B5=987.77

    // Soulful 8-Bar Romantic Acoustic Progression (Fmaj7 -> G6 -> Em7 -> Am9 -> Dm7 -> G7 -> Cmaj7 -> Fmaj9)
    // Each step: { bass, chord: [notes], melody, dur, delay }
    this.score = [
      // BAR 1: Fmaj7 (Sweet yearning warmth)
      { bass: 87.31, chord: [174.61, 261.63, 329.63], melody: 440.00, dur: 2.2, delay: 0.65 }, // A4
      { chord: [261.63, 349.23], melody: 392.00, dur: 1.8, delay: 0.65 }, // G4
      { chord: [329.63], melody: 329.63, dur: 1.6, delay: 0.65 }, // E4
      { chord: [261.63, 440.00], melody: 392.00, dur: 2.0, delay: 0.85 }, // G4

      // BAR 2: G6 / B (Gentle flowing motion)
      { bass: 98.00, chord: [196.00, 293.66, 392.00], melody: 493.88, dur: 2.2, delay: 0.65 }, // B4
      { chord: [293.66, 392.00], melody: 523.25, dur: 1.8, delay: 0.65 }, // C5
      { chord: [392.00], melody: 587.33, dur: 1.6, delay: 0.65 }, // D5
      { chord: [293.66, 493.88], melody: 493.88, dur: 2.0, delay: 0.85 }, // B4

      // BAR 3: Em7 (Tender emotional depth)
      { bass: 82.41, chord: [164.81, 246.94, 329.63], melody: 392.00, dur: 2.2, delay: 0.65 }, // G4
      { chord: [246.94, 329.63], melody: 329.63, dur: 1.8, delay: 0.65 }, // E4
      { chord: [329.63], melody: 293.66, dur: 1.6, delay: 0.65 }, // D4
      { chord: [246.94, 392.00], melody: 329.63, dur: 2.0, delay: 0.85 }, // E4

      // BAR 4: Am9 (Deep nostalgic affection)
      { bass: 110.00, chord: [220.00, 261.63, 329.63], melody: 523.25, dur: 2.2, delay: 0.65 }, // C5
      { chord: [261.63, 329.63], melody: 493.88, dur: 1.8, delay: 0.65 }, // B4
      { chord: [329.63], melody: 440.00, dur: 1.6, delay: 0.65 }, // A4
      { chord: [261.63, 440.00], melody: 392.00, dur: 2.2, delay: 0.95 }, // G4

      // BAR 5: Dm9 (Heartfelt romantic phrase)
      { bass: 73.42, chord: [146.83, 220.00, 261.63], melody: 349.23, dur: 2.2, delay: 0.65 }, // F4
      { chord: [220.00, 261.63], melody: 440.00, dur: 1.8, delay: 0.65 }, // A4
      { chord: [261.63], melody: 523.25, dur: 1.6, delay: 0.65 }, // C5
      { chord: [220.00, 349.23], melody: 587.33, dur: 2.2, delay: 0.85 }, // D5

      // BAR 6: Gsus4 -> G7 (Emotional buildup into resolve)
      { bass: 98.00, chord: [196.00, 261.63, 392.00], melody: 659.25, dur: 2.2, delay: 0.65 }, // E5
      { chord: [196.00, 246.94, 349.23], melody: 587.33, dur: 1.8, delay: 0.65 }, // D5
      { chord: [293.66], melody: 523.25, dur: 1.6, delay: 0.65 }, // C5
      { chord: [246.94, 392.00], melody: 493.88, dur: 2.0, delay: 0.85 }, // B4

      // BAR 7: Cmaj7 (Peaceful arrival and joy)
      { bass: 65.41, chord: [130.81, 196.00, 246.94, 329.63], melody: 523.25, dur: 2.4, delay: 0.70 }, // C5
      { chord: [196.00, 329.63], melody: 659.25, dur: 1.8, delay: 0.65 }, // E5
      { chord: [246.94], melody: 783.99, dur: 2.0, delay: 0.75 }, // G5
      { chord: [196.00, 329.63], melody: 659.25, dur: 2.2, delay: 0.85 }, // E5

      // BAR 8: Fmaj9 / C (Tender lingering whisper)
      { bass: 87.31, chord: [174.61, 220.00, 261.63], melody: 440.00, dur: 2.2, delay: 0.65 }, // A4
      { chord: [220.00, 261.63], melody: 392.00, dur: 1.8, delay: 0.65 }, // G4
      { chord: [261.63], melody: 349.23, dur: 1.6, delay: 0.65 }, // F4
      { chord: [174.61, 261.63], melody: 329.63, dur: 2.8, delay: 1.20 }  // E4 (lingering chord)
    ];
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioCtx();

      // Master output gain
      this.masterGain = this.ctx.createGain();
      this.masterGain.gain.setValueAtTime(this.volume, this.ctx.currentTime);

      // Warm acoustic room reverb filter
      const filter = this.ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(3200, this.ctx.currentTime);

      // Subtle warm compressor for smooth acoustic leveling
      const compressor = this.ctx.createDynamicsCompressor();
      compressor.threshold.setValueAtTime(-18, this.ctx.currentTime);
      compressor.knee.setValueAtTime(25, this.ctx.currentTime);
      compressor.ratio.setValueAtTime(3.5, this.ctx.currentTime);
      compressor.attack.setValueAtTime(0.015, this.ctx.currentTime);
      compressor.release.setValueAtTime(0.25, this.ctx.currentTime);

      this.masterGain.connect(filter);
      filter.connect(compressor);
      compressor.connect(this.ctx.destination);
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  // Synthesize an authentic acoustic piano note with fundamental + warm wooden harmonics
  playPianoNote(freq, duration, gainLevel = 0.25, isMelody = false) {
    if (!this.ctx) return;
    const now = this.ctx.currentTime;

    // Harmonic partials for rich acoustic piano timbre:
    // 1. Fundamental sine
    // 2. Warm body overtone (octave)
    // 3. Shimmer overtone (5th harmonic)
    const osc1 = this.ctx.createOscillator();
    const osc2 = this.ctx.createOscillator();
    const osc3 = this.ctx.createOscillator();

    const noteGain = this.ctx.createGain();

    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(freq, now);

    // Warm wooden body overtone (slight detune for acoustic chorus shimmer)
    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(freq * 2.003, now);

    // Shimmer upper harmonic
    osc3.type = 'sine';
    osc3.frequency.setValueAtTime(freq * 3.008, now);

    // Acoustic Piano ADSR:
    // Crisp hammer attack (0.012s), warm body swell (0.04s), slow singing decay
    const peakGain = isMelody ? gainLevel * 1.35 : gainLevel;
    noteGain.gain.setValueAtTime(0, now);
    noteGain.gain.linearRampToValueAtTime(peakGain, now + 0.012);
    noteGain.gain.exponentialRampToValueAtTime(peakGain * 0.45, now + 0.25);
    noteGain.gain.exponentialRampToValueAtTime(0.0001, now + duration);

    // Tone shaping filter for each note
    const noteFilter = this.ctx.createBiquadFilter();
    noteFilter.type = 'lowpass';
    noteFilter.frequency.setValueAtTime(isMelody ? 4200 : 2600, now);

    osc1.connect(noteGain);
    osc2.connect(noteGain);
    osc3.connect(noteGain);
    noteGain.connect(noteFilter);
    noteFilter.connect(this.masterGain);

    osc1.start(now);
    osc2.start(now);
    osc3.start(now);

    const stopTime = now + duration + 0.15;
    osc1.stop(stopTime);
    osc2.stop(stopTime);
    osc3.stop(stopTime);
  }

  step() {
    if (!this.isPlaying) return;
    const bar = this.score[this.currentStep];

    // 1. Play deep bass note if present (lingering 3.2s)
    if (bar.bass) {
      this.playPianoNote(bar.bass, 3.2, 0.26, false);
    }

    // 2. Play acoustic chord harmonies (lingering 2.4s)
    if (bar.chord && bar.chord.length) {
      bar.chord.forEach((n, idx) => {
        setTimeout(() => {
          if (this.isPlaying) this.playPianoNote(n, 2.4, 0.14, false);
        }, idx * 45); // subtle rolling strum
      });
    }

    // 3. Play singing emotional lead melody
    if (bar.melody) {
      setTimeout(() => {
        if (this.isPlaying) this.playPianoNote(bar.melody, bar.dur, 0.28, true);
      }, 70);
    }

    this.currentStep = (this.currentStep + 1) % this.score.length;
    this.timer = setTimeout(() => this.step(), bar.delay * 1000);
  }

  start() {
    this.init();
    if (this.isPlaying) return;
    this.isPlaying = true;

    // Check if custom MP3 is available
    if (!this.audioElement) {
      this.audioElement = new Audio('assets/audio/bgm.mp3');
      this.audioElement.loop = true;
      this.audioElement.volume = this.volume;
    }

    // Attempt to play custom MP3 if present, otherwise play rich acoustic piano synthesizer
    const playPromise = this.audioElement.play();
    if (playPromise !== undefined) {
      playPromise.then(() => {
        // Custom MP3 is playing!
        this.onStateChange(true, "Music Playing ♪");
      }).catch(() => {
        // Fallback to our soulful acoustic piano melody
        this.step();
        this.onStateChange(true, "Romantic Melody ♪");
      });
    } else {
      this.step();
      this.onStateChange(true, "Romantic Melody ♪");
    }
  }

  stop() {
    this.isPlaying = false;
    if (this.audioElement) {
      this.audioElement.pause();
    }
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    this.onStateChange(false);
  }

  toggle() {
    if (this.isPlaying) {
      this.stop();
    } else {
      this.start();
    }
    return this.isPlaying;
  }

  onStateChange(isPlaying, customText) {
    const btn = document.getElementById('music-toggle-btn');
    const label = document.getElementById('music-label');
    const waves = document.querySelectorAll('.music-wave-bar');
    if (btn) {
      btn.setAttribute('aria-pressed', isPlaying ? 'true' : 'false');
    }
    if (label) {
      label.textContent = isPlaying ? (customText || "Romantic Melody ♪") : "Music Paused";
    }
    waves.forEach(w => {
      w.style.animationPlayState = isPlaying ? 'running' : 'paused';
    });
  }

  // Celebratory birthday chime chords for cake cutting
  playCelebrationChime() {
    this.init();
    const chords = [
      { note: 261.63, dur: 1.4 }, // C4
      { note: 329.63, dur: 1.4 }, // E4
      { note: 392.00, dur: 1.6 }, // G4
      { note: 523.25, dur: 1.8 }, // C5
      { note: 659.25, dur: 2.2 }, // E5
      { note: 783.99, dur: 2.6 }, // G5
      { note: 1046.50, dur: 3.2 } // C6
    ];
    chords.forEach((c, idx) => {
      setTimeout(() => {
        this.playPianoNote(c.note, c.dur, 0.32, true);
      }, idx * 110);
    });
  }
}

window.ambientMusic = new RomanticPianoEngine();
