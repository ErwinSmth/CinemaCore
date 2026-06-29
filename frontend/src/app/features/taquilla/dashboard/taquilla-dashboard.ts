import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-taquilla-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './taquilla-dashboard.html',
})
export class TaquillaDashboard {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
