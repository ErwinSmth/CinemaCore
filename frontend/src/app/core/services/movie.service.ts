import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface MovieCarteleraResponse {
  id: number;
  titulo: string;
  poster_path: string;
  estado: string;
  restriccion_edad: string;
}

@Injectable({
  providedIn: 'root'
})
export class MovieService {
  private readonly API_URL = 'http://localhost:8080/api/v1/movies';

  // Signals para manejar el estado
  cartelera = signal<MovieCarteleraResponse[]>([]);
  isLoading = signal<boolean>(false);

  constructor(private http: HttpClient) {}

  fetchCartelera(): Observable<MovieCarteleraResponse[]> {
    this.isLoading.set(true);
    return this.http.get<MovieCarteleraResponse[]>(this.API_URL).pipe(
      tap((movies) => {
        this.cartelera.set(movies);
        this.isLoading.set(false);
      })
    );
  }
}
