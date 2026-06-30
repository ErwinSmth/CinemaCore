import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovieService, Movie, TmdbMovie, UpdateMovieRequest } from '../../../core/services/movie.service';

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-movies.html',
})
export class AdminMovies implements OnInit {
  private movieService = inject(MovieService);

  // Estado del Catálogo Local
  movies = signal<Movie[]>([]);
  isLoadingMovies = signal<boolean>(false);
  activeFilter = signal<string>(''); // Vacio = Todos

  // Estado Modal TMDB
  isTmdbModalOpen = signal<boolean>(false);
  tmdbSearchQuery = signal<string>('');
  tmdbResults = signal<TmdbMovie[]>([]);
  isSearchingTmdb = signal<boolean>(false);
  isImporting = signal<boolean>(false);

  // Estado Modal Edición
  isEditModalOpen = signal<boolean>(false);
  editingMovie = signal<Movie | null>(null);
  isSaving = signal<boolean>(false);
  editForm = signal<UpdateMovieRequest>({ titulo: '', sinopsis: '', estado: '' });

  ngOnInit() {
    this.loadMovies();
  }

  loadMovies() {
    this.isLoadingMovies.set(true);
    this.movieService.getAdminMovies(this.activeFilter() || undefined).subscribe({
      next: (data) => {
        this.movies.set(data);
        this.isLoadingMovies.set(false);
      },
      error: (err) => {
        console.error('Error cargando películas admin', err);
        this.isLoadingMovies.set(false);
      }
    });
  }

  setFilter(status: string) {
    this.activeFilter.set(status);
    this.loadMovies();
  }

  // --- LOGICA TMDB ---

  openTmdbModal() {
    this.isTmdbModalOpen.set(true);
    this.tmdbSearchQuery.set('');
    this.tmdbResults.set([]);
  }

  closeTmdbModal() {
    this.isTmdbModalOpen.set(false);
  }

  searchTmdb() {
    if (!this.tmdbSearchQuery().trim()) return;
    this.isSearchingTmdb.set(true);
    this.movieService.searchTmdbMovies(this.tmdbSearchQuery()).subscribe({
      next: (results) => {
        this.tmdbResults.set(results);
        this.isSearchingTmdb.set(false);
      },
      error: (err) => {
        console.error('Error buscando en TMDB', err);
        this.isSearchingTmdb.set(false);
      }
    });
  }

  importFromTmdb(tmdbId: number) {
    this.isImporting.set(true);
    this.movieService.importMovie(tmdbId).subscribe({
      next: (res) => {
        this.closeTmdbModal();
        this.isLoadingMovies.set(true);
        // Encadenamos la recarga de películas para asegurar que tengamos los datos actualizados
        this.movieService.getAdminMovies(this.activeFilter() || undefined).subscribe({
          next: (data) => {
            this.movies.set(data);
            this.isLoadingMovies.set(false);
            this.isImporting.set(false);
            const imported = data.find(m => m.tmdbId === tmdbId);
            if (imported) this.openEditModal(imported);
          },
          error: (err) => {
            console.error('Error recargando películas tras importación', err);
            this.isLoadingMovies.set(false);
            this.isImporting.set(false);
          }
        });
      },
      error: (err) => {
        console.error('Error importando película', err);
        alert(err.error?.message || 'Error importando la película.');
        this.isImporting.set(false);
      }
    });
  }

  // --- LOGICA EDICIÓN ---

  openEditModal(movie: Movie) {
    this.editingMovie.set(movie);
    this.editForm.set({
      titulo: movie.titulo,
      sinopsis: movie.sinopsis,
      estado: movie.estado,
      restriccionCodigo: movie.restriccionEdad?.codigo || 'APT'
    });
    this.isEditModalOpen.set(true);
  }

  closeEditModal() {
    this.isEditModalOpen.set(false);
    this.editingMovie.set(null);
  }

  setEditEstado(estado: string) {
    this.editForm.update(f => ({ ...f, estado }));
  }

  setEditRestriccion(codigo: string) {
    this.editForm.update(f => ({ ...f, restriccionCodigo: codigo }));
  }

  updateEditTitulo(titulo: string) {
    this.editForm.update(f => ({ ...f, titulo }));
  }

  updateEditSinopsis(sinopsis: string) {
    this.editForm.update(f => ({ ...f, sinopsis }));
  }

  getPosterPath(movie: any): string | null {
    if (!movie) return null;
    return movie.posterPath || null;
  }

  saveMovie() {
    const movie = this.editingMovie();
    if (!movie) return;

    this.isSaving.set(true);
    this.movieService.updateMovie(movie.id, this.editForm()).subscribe({
      next: (updatedMovie) => {
        this.isSaving.set(false);
        this.closeEditModal();
        this.loadMovies();
        // Si la peli pasó a CARTELERA o PRE-ESTRENO, refrescamos el cache publico 
        // llamando silenciosamente a fetchCartelera (aunque el backend ya limpió Redis)
        this.movieService.fetchCartelera().subscribe();
        this.movieService.fetchPreEstrenos().subscribe();
      },
      error: (err) => {
        console.error('Error actualizando película', err);
        alert(err.error?.message || 'Error actualizando la película.');
        this.isSaving.set(false);
      }
    });
  }

  deleteMovie(id: number) {
    if (!confirm('¿Estás seguro de eliminar lógicamente esta película?')) return;
    this.movieService.deleteMovie(id).subscribe({
      next: () => {
        this.loadMovies();
        this.movieService.fetchCartelera().subscribe();
        this.movieService.fetchPreEstrenos().subscribe();
      },
      error: (err) => {
        alert(err.error?.message || 'Error eliminando la película.');
      }
    });
  }
}
