package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.model.Joke;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JokeService {

    private final List<Joke> jokes = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);
    private final Random random = new Random();

    public JokeService() {
        jokes.add(new Joke(counter.getAndIncrement(), "L'école",
                "Un élève dit à son prof : « J'ai pas fait mes devoirs car j'ai perdu ma mémoire. »\nLe prof : « Depuis quand ? »\nL'élève : « Depuis quand quoi ? »", 6.5));
        jokes.add(new Joke(counter.getAndIncrement(), "Le médecin",
                "Patient : « Docteur, je me sens pas bien. »\nMédecin : « Déshabilles-vous. »\nPatient : « Mais c'est par téléphone ! »\nMédecin : « Ah... restez en ligne. »", 8.5));
        jokes.add(new Joke(counter.getAndIncrement(), "Le programmeur",
                "Pourquoi les programmeurs préfèrent le noir ?\nParce que la lumière attire les bugs !", 9.0));
        jokes.add(new Joke(counter.getAndIncrement(), "L'hôpital",
                "Malade : « J'ai avalé un stylo ! »\nInfirmière : « J'arrive, qu'est-ce que vous faites en attendant ? »\nMalade : « J'utilise un crayon. »", 3.0));
        jokes.add(new Joke(counter.getAndIncrement(), "La mémoire",
                "Ma femme dit que j'ai une mauvaise mémoire.\nJ'étais surpris... pour la 3ème fois ce soir.", 2.0));
    }
    public List<Joke> getAll() {
        return jokes;
    }

    public Optional<Joke> getById(Long id) {
        return jokes.stream().filter(j -> j.getId().equals(id)).findFirst();
    }

    public Optional<Joke> getByTitle(String title) {
        return jokes.stream()
                .filter(j -> j.getTitle().toLowerCase().contains(title.toLowerCase()))
                .findFirst();
    }

    public Joke getRandom() {
        return jokes.get(random.nextInt(jokes.size()));
    }


    public Optional<Joke> getWorst() {
        return jokes.stream().min(Comparator.comparingDouble(Joke::getRating));
    }

    public Optional<Joke> getBest() {
        return jokes.stream().max(Comparator.comparingDouble(Joke::getRating));
    }


    public Joke add(String title, String text, double rating) {
        Joke joke = new Joke(counter.getAndIncrement(), title, text, rating);
        jokes.add(joke);
        return joke;
    }

    public Optional<Joke> update(Long id, String title, String text, double rating) {
        Optional<Joke> opt = getById(id);
        opt.ifPresent(j -> {
            j.setTitle(title);
            j.setText(text);
            j.setRating(rating);
        });
        return opt;
    }

    public Optional<Joke> rate(Long id, double newRating) {
        Optional<Joke> opt = getById(id);
        opt.ifPresent(j -> j.setRating(newRating));
        return opt;
    }

    public boolean delete(Long id) {
        return jokes.removeIf(j -> j.getId().equals(id));
    }
}