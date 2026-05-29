package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.model.OmdbResponse;
import fr.ensim.interop.introrest.model.generated.FilmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FilmService {

    @Value("${omdb.api.key}")
    private String apiKey;

    @Value("${omdb.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public FilmResponse searchFilm(String titre) {
        String url = baseUrl + "?t={titre}&plot=full&apikey={key}";

        OmdbResponse omdb = restTemplate.getForObject(
                url, OmdbResponse.class, titre, apiKey
        );

        if (omdb == null || !omdb.isFound()) {
            throw new RuntimeException(
                    omdb != null ? omdb.getError() : "Film introuvable : " + titre
            );
        }

        return mapToFilmResponse(omdb);
    }

    private FilmResponse mapToFilmResponse(OmdbResponse omdb) {
        FilmResponse film = new FilmResponse();
        film.setTitre(omdb.getTitle());
        film.setSynopsis(omdb.getPlot());
        film.setAnnee(omdb.getYear());
        film.setAffiche(omdb.getPoster());
        try {
            film.setNote(Double.parseDouble(omdb.getImdbRating()));
        } catch (NumberFormatException e) {
            film.setNote(0.0);
        }
        return film;
    }

    public String formatCaption(FilmResponse film) {
        return film.getTitre() + " (" + film.getAnnee() + ")\n"
                + "Note IMDB : " + film.getNote() + "/10\n\n"
                + film.getSynopsis();
    }
}