import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TtsService {
  isPlaying = false;

  toggle(text: string): void {
    if (this.isPlaying) {
      this.stop();
      return;
    }
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      return;
    }
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.onend = () => {
      this.isPlaying = false;
    };
    this.isPlaying = true;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  }

  stop(): void {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    this.isPlaying = false;
  }

  clearCache(): void {}
}

