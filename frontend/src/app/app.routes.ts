import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { Home } from './features/public/home/home';
import { Dashboard } from './features/admin/dashboard/dashboard';

export const routes: Routes = [
  { path: '', component: Home, pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'admin', component: Dashboard },
  { path: '**', redirectTo: '' }
];
