import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
})
export class Home {
  private authService = inject(AuthService);

  isAuthenticated = this.authService.isAuthenticated;
  
  // Usamos computed para derivar el estado de admin
  isAdmin = computed(() => {
    return this.authService.currentUserRoles().includes('ROLE_ADMINISTRADOR');
  });

  logout() {
    this.authService.logout();
  }
}
