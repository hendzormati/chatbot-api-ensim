package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.api.BlagueApi;
import fr.ensim.interop.introrest.model.generated.Joke;
import fr.ensim.interop.introrest.model.generated.JokeRequest;
import fr.ensim.interop.introrest.model.generated.RateRequest;
import fr.ensim.interop.introrest.service.JokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JokeController implements BlagueApi {

    @Autowired
    private JokeService jokeService;

    @Override
    public ResponseEntity<List<Joke>> getAllJokes() {
        return ResponseEntity.ok(jokeService.getAll());
    }

    @Override
    public ResponseEntity<Joke> getRandomJoke() {
        return ResponseEntity.ok(jokeService.getRandom());
    }

    @Override
    public ResponseEntity<Joke> getBestJoke() {
        return jokeService.getBest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Joke> getWorstJoke() {
        return jokeService.getWorst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Joke> getJokeById(Long id) {
        return jokeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<Joke>> getJokeByTitle(String titre) {
        return jokeService.getByTitle(titre)
                .map(joke -> ResponseEntity.ok(List.of(joke)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Joke> addJoke(JokeRequest jokeRequest) {
        Joke created = jokeService.add(
                jokeRequest.getTitle(),
                jokeRequest.getText(),
                jokeRequest.getRating());
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Joke> updateJoke(Long id, JokeRequest jokeRequest) {
        return jokeService.update(id, jokeRequest.getTitle(),
                jokeRequest.getText(), jokeRequest.getRating())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Joke> rateJoke(Long id, RateRequest rateRequest) {
        return jokeService.rate(id, rateRequest.getRating())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteJoke(Long id) {
        return jokeService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}