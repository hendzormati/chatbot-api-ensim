package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.Joke;
import fr.ensim.interop.introrest.service.JokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jokes")
public class JokeController {

    @Autowired
    private JokeService jokeService;

    @GetMapping
    public List<Joke> getAll() {
        return jokeService.getAll();
    }

    @GetMapping("/random")
    public ResponseEntity<Joke> getRandom() {
        return ResponseEntity.ok(jokeService.getRandom());
    }

    @GetMapping("/best")
    public ResponseEntity<?> getBest() {
        return jokeService.getBest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/worst")
    public ResponseEntity<?> getWorst() {
        return jokeService.getWorst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return jokeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Joke> add(@RequestBody Map<String, Object> body) {
        String title  = (String) body.get("title");
        String text   = (String) body.get("text");
        double rating = ((Number) body.get("rating")).doubleValue();
        return ResponseEntity.status(201).body(jokeService.add(title, text, rating));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title  = (String) body.get("title");
        String text   = (String) body.get("text");
        double rating = ((Number) body.get("rating")).doubleValue();
        return jokeService.update(id, title, text, rating)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/rate")
    public ResponseEntity<?> rate(@PathVariable Long id, @RequestBody Map<String, Number> body) {
        double newRating = body.get("rating").doubleValue();
        return jokeService.rate(id, newRating)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return jokeService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}