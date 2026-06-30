import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MovieService, MovieDetailResponse } from '../../../core/services/movie.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './movie-detail.component.html',
  styleUrls: ['./movie-detail.component.css']
})
export class MovieDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private movieService = inject(MovieService);
  private sanitizer = inject(DomSanitizer);
  private location = inject(Location);

  movie = signal<MovieDetailResponse | null>(null);
  isLoading = signal<boolean>(true);
  error = signal<string | null>(null);

  // Trailer
  trailerUrl = signal<SafeResourceUrl | null>(null);
  isTrailerOpen = signal<boolean>(false);

  // Expanded details
  showMoreDetails = signal<boolean>(false);

  // Age Restriction dictionary
  private ageRestrictionDict: Record<string, string> = {
    'APT': 'Apto para todo público',
    '7+': 'Mayores de 7 años',
    '12+': 'Mayores de 12 años',
    '14+': 'Mayores de 14 años',
    '16+': 'Mayores de 16 años',
    '18+': 'Mayores de 18 años / Apto para público adulto'
  };

  formattedAgeRestriction = computed(() => {
    const code = this.movie()?.restriccion_edad || '';
    return this.ageRestrictionDict[code] || code;
  });

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadMovie(parseInt(id, 10));
      }
    });
  }

  private loadMovie(id: number) {
    this.isLoading.set(true);
    this.movieService.getMovieById(id).subscribe({
      next: (data) => {
        this.movie.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching movie details:', err);
        this.error.set('No se pudo cargar la película.');
        this.isLoading.set(false);
      }
    });
  }

  goBack() {
    this.location.back();
  }

  toggleMoreDetails() {
    this.showMoreDetails.update(v => !v);
  }

  scrollToFunctions() {
    const el = document.getElementById('funciones-section');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  }

  formatDuration(mins: number | undefined): string {
    if (!mins) return 'N/A';
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return `${h}h ${m}m`;
  }

  openTrailer() {
    const movieData = this.movie();
    if (!movieData?.trailers?.length) return;
    const url = movieData.trailers[0];
    const videoId = this.extractYoutubeId(url);
    if (!videoId) return;
    const embedUrl = `https://www.youtube.com/embed/${videoId}?autoplay=1&rel=0`;
    this.trailerUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl));
    this.isTrailerOpen.set(true);
  }

  closeTrailer() {
    this.isTrailerOpen.set(false);
    setTimeout(() => this.trailerUrl.set(null), 300);
  }

  private extractYoutubeId(url: string): string | null {
    const match = url.match(/(?:v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/);
    return match ? match[1] : null;
  }
}
