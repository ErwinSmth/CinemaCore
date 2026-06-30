import { Component, computed, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MovieService } from '../../../core/services/movie.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private movieService = inject(MovieService);

  // Computados
  carteleraCount = computed(() => this.movieService.cartelera().length);
  preEstrenosCount = computed(() => this.movieService.preEstrenos().length);

  // Computed para saber si estamos en la raíz del admin
  isDashboardHome = computed(() => this.router.url === '/admin');

  ngOnInit() {
    if (this.movieService.cartelera().length === 0) {
      this.movieService.fetchCartelera().subscribe();
    }
    if (this.movieService.preEstrenos().length === 0) {
      this.movieService.fetchPreEstrenos().subscribe();
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
