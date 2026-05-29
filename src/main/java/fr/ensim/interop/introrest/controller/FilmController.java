package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.api.FilmApi;
import fr.ensim.interop.introrest.model.generated.FilmResponse;
import fr.ensim.interop.introrest.service.FilmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FilmController implements FilmApi {

    @Autowired
    private FilmService filmService;

    @Override
    public ResponseEntity<FilmResponse> searchFilm(String titre) {
        try {
            return ResponseEntity.ok(filmService.searchFilm(titre));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}