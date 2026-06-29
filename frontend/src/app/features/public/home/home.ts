import { Component, computed, inject, OnInit, OnDestroy, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../../core/services/auth.service';
import { MovieService, MovieCarteleraResponse } from '../../../core/services/movie.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
})
export class Home implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private movieService = inject(MovieService);
  private sanitizer = inject(DomSanitizer);
  private cdr = inject(ChangeDetectorRef);

  isAuthenticated = this.authService.isAuthenticated;
  cartelera = this.movieService.cartelera;
  preEstrenos = this.movieService.preEstrenos;
  isLoadingCartelera = this.movieService.isLoadingCartelera;
  isLoadingPreEstrenos = this.movieService.isLoadingPreEstrenos;

  isAdmin = computed(() => this.authService.currentUserRoles().includes('ROLE_ADMINISTRADOR'));

  // Hero Banner state
  heroIndex = signal<number>(0);
  private heroTimer: any;

  // Hero movies: combina cartelera + preEstrenos ordenados por fecha_estreno descendente, toma top 4
  heroMovies = computed<MovieCarteleraResponse[]>(() => {
    const all = [...this.cartelera(), ...this.preEstrenos()].filter(m => m.backdrop_path);
    return all
      .sort((a, b) => {
        const dA = a.fecha_estreno ? new Date(a.fecha_estreno).getTime() : 0;
        const dB = b.fecha_estreno ? new Date(b.fecha_estreno).getTime() : 0;
        return dB - dA;
      })
      .slice(0, 4);
  });

  currentHeroMovie = computed<MovieCarteleraResponse | null>(() => {
    const movies = this.heroMovies();
    if (!movies.length) return null;
    return movies[this.heroIndex() % movies.length];
  });

  // Trailer modal state
  trailerUrl = signal<SafeResourceUrl | null>(null);
  isModalOpen = signal<boolean>(false);

  // Computed state for Featured Trailers (prioritize pre-estrenos, then cartelera)
  featuredTrailers = computed(() => {
    const pre = this.preEstrenos().filter(m => m.trailers && m.trailers.length > 0);
    const cart = this.cartelera().filter(m => m.trailers && m.trailers.length > 0);
    return [...pre, ...cart].slice(0, 10); // Show up to 10 trailers
  });

  // Quick Book state
  activeQuickBookTab = signal<'pelicula' | 'fecha' | 'hora' | null>(null);

  ngOnInit() {
    if (this.cartelera().length === 0) {
      this.movieService.fetchCartelera().subscribe({
        next: () => setTimeout(() => this.cdr.detectChanges(), 0),
        error: (err) => {
          console.error('Error fetching cartelera:', err);
          setTimeout(() => this.cdr.detectChanges(), 0);
        }
      });
    }
    if (this.preEstrenos().length === 0) {
      this.movieService.fetchPreEstrenos().subscribe({
        next: () => setTimeout(() => this.cdr.detectChanges(), 0),
        error: (err) => {
          console.error('Error fetching pre-estrenos:', err);
          setTimeout(() => this.cdr.detectChanges(), 0);
        }
      });
    }
    this.startHeroAutoPlay();
  }

  ngOnDestroy() {
    this.stopHeroAutoPlay();
  }

  private startHeroAutoPlay() {
    this.heroTimer = setInterval(() => {
      const total = this.heroMovies().length;
      if (total > 1) {
        this.heroIndex.update(i => (i + 1) % total);
      }
    }, 5000);
  }

  private stopHeroAutoPlay() {
    if (this.heroTimer) clearInterval(this.heroTimer);
  }

  goToHeroSlide(index: number) {
    this.heroIndex.set(index);
    this.stopHeroAutoPlay();
    this.startHeroAutoPlay();
  }

  nextHeroSlide() {
    const total = this.heroMovies().length;
    if (!total) return;
    this.heroIndex.update(i => (i + 1) % total);
    this.stopHeroAutoPlay();
    this.startHeroAutoPlay();
  }

  prevHeroSlide() {
    const total = this.heroMovies().length;
    if (!total) return;
    this.heroIndex.update(i => (i - 1 + total) % total);
    this.stopHeroAutoPlay();
    this.startHeroAutoPlay();
  }

  openTrailer(movie: MovieCarteleraResponse) {
    if (!movie.trailers?.length) return;
    const url = movie.trailers[0];
    // Convertir URL de YouTube a formato embed seguro
    const videoId = this.extractYoutubeId(url);
    if (!videoId) return;
    const embedUrl = `https://www.youtube.com/embed/${videoId}?autoplay=1&rel=0`;
    this.trailerUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl));
    this.isModalOpen.set(true);
  }

  closeTrailer() {
    this.isModalOpen.set(false);
    setTimeout(() => this.trailerUrl.set(null), 300);
  }

  getYoutubeThumbnail(url: string): string {
    if (!url) return '';
    const match = url.match(/[?&]v=([^&]+)/) || url.match(/youtu\.be\/([^?]+)/);
    const videoId = match ? match[1] : null;
    return videoId ? `https://img.youtube.com/vi/${videoId}/mqdefault.jpg` : '';
  }

  toggleQuickBookTab(tab: 'pelicula' | 'fecha' | 'hora') {
    if (this.activeQuickBookTab() === tab) {
      this.activeQuickBookTab.set(null);
    } else {
      this.activeQuickBookTab.set(tab);
    }
  }

  closeQuickBook() {
    this.activeQuickBookTab.set(null);
  }

  private extractYoutubeId(url: string): string | null {
    const match = url.match(/(?:v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/);
    return match ? match[1] : null;
  }

  logout() {
    this.authService.logout();
  }
}
