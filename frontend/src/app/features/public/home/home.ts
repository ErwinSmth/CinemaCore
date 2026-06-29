import { Component, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MovieService } from '../../../core/services/movie.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
})
export class Home implements OnInit {
  private authService = inject(AuthService);
  private movieService = inject(MovieService);

  isAuthenticated = this.authService.isAuthenticated;
  movies = this.movieService.cartelera;
  isLoading = this.movieService.isLoading;
  
  // Usamos computed para derivar el estado de admin
  isAdmin = computed(() => {
    return this.authService.currentUserRoles().includes('ROLE_ADMINISTRADOR');
  });

  ngOnInit() {
    // Solo cargamos la cartelera si aún no está cargada (evitar llamadas dobles si navegan de vuelta)
    if (this.movies().length === 0) {
      this.movieService.fetchCartelera().subscribe({
        error: (err) => console.error('Error fetching cartelera:', err)
      });
    }
  }

  logout() {
    this.authService.logout();
  }
}
