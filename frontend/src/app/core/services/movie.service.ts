import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, EMPTY } from 'rxjs';
import { tap, finalize, catchError } from 'rxjs/operators';

export interface MovieCarteleraResponse {
  id: number;
  titulo: string;
  poster_path: string;
  backdrop_path?: string;
  estado: string;
  restriccion_edad: string;
  fecha_estreno?: string;
  trailers?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class MovieService {
  private readonly API_URL = 'http://localhost:8080/api/v1/movies';

  cartelera = signal<MovieCarteleraResponse[]>([]);
  preEstrenos = signal<MovieCarteleraResponse[]>([]);
  isLoadingCartelera = signal<boolean>(false);
  isLoadingPreEstrenos = signal<boolean>(false);

  constructor(private http: HttpClient) {}

  fetchCartelera(): Observable<MovieCarteleraResponse[]> {
    this.isLoadingCartelera.set(true);
    return this.http.get<MovieCarteleraResponse[]>(this.API_URL).pipe(
      tap((movies) => this.cartelera.set(movies)),
      catchError((err) => {
        console.error('Error fetching cartelera:', err);
        return EMPTY;
      }),
      finalize(() => this.isLoadingCartelera.set(false))
    );
  }

  fetchPreEstrenos(): Observable<MovieCarteleraResponse[]> {
    this.isLoadingPreEstrenos.set(true);
    return this.http.get<MovieCarteleraResponse[]>(`${this.API_URL}/pre-estreno`).pipe(
      tap((movies) => this.preEstrenos.set(movies)),
      catchError((err) => {
        console.error('Error fetching pre-estrenos:', err);
        return EMPTY;
      }),
      finalize(() => this.isLoadingPreEstrenos.set(false))
    );
  }
}
