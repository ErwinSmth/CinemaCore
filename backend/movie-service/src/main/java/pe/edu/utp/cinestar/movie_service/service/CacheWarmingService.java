package pe.edu.utp.cinestar.movie_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheWarmingService {

    private final MovieService movieService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        movieService.getCartelera(null, null);
        movieService.getPreEstrenos(null, null);
    }
}

