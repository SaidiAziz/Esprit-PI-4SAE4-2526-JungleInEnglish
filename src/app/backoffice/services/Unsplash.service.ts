import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface UnsplashImage {
  url: string;
  alt?: string;
}

@Injectable({ providedIn: 'root' })
export class UnsplashService {
  private cache = new Map<string, UnsplashImage>();

  getImageForQuestion(query: string): Observable<UnsplashImage> {
    const key = (query || 'english').toLowerCase();
    if (!this.cache.has(key)) {
      this.cache.set(key, {
        url: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=1200&q=80',
        alt: key
      });
    }
    return of(this.cache.get(key)!);
  }

  clearCache(): void {
    this.cache.clear();
  }
}

