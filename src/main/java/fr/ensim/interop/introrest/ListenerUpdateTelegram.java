package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.model.generated.FilmResponse;
import fr.ensim.interop.introrest.model.generated.ForecastDay;
import fr.ensim.interop.introrest.model.generated.ForecastResponse;
import fr.ensim.interop.introrest.model.generated.Joke;
import fr.ensim.interop.introrest.model.telegram.Update;
import fr.ensim.interop.introrest.service.FilmService;
import fr.ensim.interop.introrest.service.JokeService;
import fr.ensim.interop.introrest.service.TelegramService;
import fr.ensim.interop.introrest.service.WeatherService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class ListenerUpdateTelegram {

	@Autowired
	private TelegramService telegramService;

	@Autowired
	private JokeService jokeService;
	@Autowired
	private WeatherService weatherService;
	@Autowired
	private FilmService filmService;
	private long lastUpdateId = 0;

	@PostConstruct
	public void startPolling() {
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				poll();
			}
		}, 0, 2000);
	}

	private void poll() {
		try {
			List<Update> updates = telegramService.getUpdates(lastUpdateId + 1);

			for (Update update : updates) {
				if (update.getUpdateId() > lastUpdateId) {
					lastUpdateId = update.getUpdateId();
				}
				if (update.getMessage() == null || update.getMessage().getText() == null) {
					continue;
				}

				handleMessage(update);
			}
		} catch (Exception e) {
			System.err.println("[Polling] Erreur : " + e.getMessage());
		}
	}

	private String extractCity(String text) {
		// "meteo Paris" → "Paris", "météo le mans" → "le mans", "prévisions Paris" →
		// "Paris"
		String cleaned = text
				.replace("prévisions", "previsions")
				.replace("previsions", "")
				.replace("météo", "meteo")
				.replace("meteo", "")
				.trim();
		return cleaned.isEmpty() ? "Paris" : cleaned; // Paris par défaut
	}

	private void handleMessage(Update update) {
		String chatId = String.valueOf(update.getMessage().getChat().getId());
		String text = update.getMessage().getText().toLowerCase().trim();

		try {
			if (text.startsWith("meteo") || text.startsWith("météo")) {
				String ville = extractCity(text);
				try {
					Meteo meteo = weatherService.getMeteoByCity(ville);
					reply(update, weatherService.formatForTelegram(ville, meteo));
				} catch (RuntimeException e) {
					reply(update, e.getMessage());
				}

			} else if (text.equals("/start")) {
				reply(update,
						"Bonjour ! Voici ce que je sais faire :\n\n"
								+ "• *meteo Paris* : météo d'une ville 🌤\n"
								+ "• *previsions Paris* : prévisions 2 jours \n"
								+ "• *film Inception* : synopsis + note + affiche\n"
								+ "• *blague* : blague aléatoire\n"
								+ "• *bonne blague* : la meilleure \n"
								+ "• *blague nulle* : la pire \n"
								+ "• *voir blague 3* : blague par id\n"
								+ "• *noter blague 3 note 8* : noter une blague\n"
								+ "• *supprimer blague 3* : supprimer\n"
								+ "• *titre blague <motclé>* : chercher une blague par titre\n"
								+ "• *ajouter blague titre | texte | note* : créer une blague\n"
								+ "• *modifier blague 3 | titre | texte | note* : modifier une blague existante\n");
			} else if (text.startsWith("previsions") || text.startsWith("prévisions") || text.startsWith("forecast")) {
				String ville = extractCity(text);
				try {
					ForecastResponse forecast = weatherService.getForecast(ville);
					reply(update, formatForecast(forecast));
				} catch (RuntimeException e) {
					reply(update,  e.getMessage());
				}
			} else if (text.startsWith("titre blague") || text.startsWith("title blague")) {
				String title = text
						.replaceFirst("^titre blague", "")
						.replaceFirst("^title blague", "")
						.trim();
				if (title.isEmpty()) {
					reply(update, "⚠ Utilise : titre blague <mot-clé>");
					return;
				}
				jokeService.getByTitle(title)
						.ifPresentOrElse(
								joke -> reply(update, formatJoke(joke)),
								() -> reply(update, "Aucune blague trouvée pour \"" + title + "\""));
			} else if (text.startsWith("ajouter blague")) {
				String payload = text.substring("ajouter blague".length()).trim();
				String[] parts = payload.split("\\|", 3);
				if (parts.length < 3) {
					reply(update, "⚠ Format invalide. Exemple : ajouter blague titre | texte | note");
					return;
				}
				try {
					String title = parts[0].trim();
					String jokeText = parts[1].trim();
					double rating = Double.parseDouble(parts[2].trim());
					Joke joke = jokeService.add(title, jokeText, rating);
					reply(update, "Blague ajoutée !\n\n" + formatJoke(joke));
				} catch (NumberFormatException e) {
					reply(update, "⚠ La note doit être un nombre. Exemple : ajouter blague titre | texte | 7.5");
				}
			} else if (text.startsWith("modifier blague") || text.startsWith("update blague")) {
				String payload = text
						.replaceFirst("^modifier blague", "")
						.replaceFirst("^update blague", "")
						.trim();
				String[] parts = payload.split("\\|", 4);
				if (parts.length < 4) {
					reply(update, "⚠ Format invalide. Exemple : modifier blague 3 | titre | texte | note");
					return;
				}
				try {
					long id = Long.parseLong(parts[0].trim());
					String title = parts[1].trim();
					String jokeText = parts[2].trim();
					double rating = Double.parseDouble(parts[3].trim());
					jokeService.update(id, title, jokeText, rating)
							.ifPresentOrElse(
									joke -> reply(update, "Blague modifiée !\n\n" + formatJoke(joke)),
									() -> reply(update, "Blague #" + id + " introuvable."));
				} catch (NumberFormatException e) {
					reply(update,
							"⚠ L'id et la note doivent être des nombres. Exemple : modifier blague 3 | titre | texte | 8.0");
				}
			}
			 else if (text.contains("blague")
					&& (text.contains("nulle") || text.contains("mauvaise") || text.contains("pire"))) {
				jokeService.getWorst().ifPresent(joke -> reply(update, formatJoke(joke)));
			} else if (text.contains("blague") && (text.contains("bonne") || text.contains("meilleure"))) {
				jokeService.getBest().ifPresent(joke -> reply(update, formatJoke(joke)));
			} else if (text.contains("blague")
					&& (text.contains("voir") || text.contains("afficher") || text.contains("montrer"))) {
				String[] parts = text.split("\\s+");
				long id = Long.parseLong(parts[parts.length - 1]);
				jokeService.getById(id)
						.ifPresentOrElse(
								joke -> reply(update, formatJoke(joke)),
								() -> reply(update, "Blague #" + id + " introuvable."));

			} else if (text.contains("blague") && (text.contains("supprimer") || text.contains("effacer"))) {
				String[] parts = text.split("\\s+");
				long id = Long.parseLong(parts[parts.length - 1]);
				boolean deleted = jokeService.delete(id);
				reply(update,
						deleted ? " Blague #" + id + " supprimée." : "Blague #" + id + " introuvable.");

			} else if (text.startsWith("noter blague")) {
				// format attendu : "noter blague {id} note {note}"
				String[] parts = text.split("\\s+");
				long id = Long.parseLong(parts[2]);
				double rating = Double.parseDouble(parts[parts.length - 1]);
				jokeService.rate(id, rating)
						.ifPresentOrElse(
								joke -> reply(update,
										" Blague *" + joke.getTitle() + "* notée " + rating + "/10"),
								() -> reply(update, " Blague #" + id + " introuvable."));

			} else if (text.contains("blague")) {
				reply(update, formatJoke(jokeService.getRandom()));
			}else if (text.startsWith("film ")) {
				String titre = text.substring(5).trim();
				try {
					FilmResponse film = filmService.searchFilm(titre);
					String caption = filmService.formatCaption(film);
					String poster = film.getAffiche();
					if (poster != null && !poster.equals("N/A")) {
						replyPhoto(update, poster, caption);
					} else {
						reply(update,caption);
					}

				} catch (RuntimeException e) {
					reply(update, "Film introuvable : " + titre);
				}
			}
			 else reply(update,
					"Pardon je ne comprends pas  ! Voici tout ce que je sais faire :\n\n"
							+ "• *meteo Paris* : météo d'une ville 🌤\n"
							+ "• *previsions Paris* : prévisions 2 jours \n"
							+ "• *film Inception* : synopsis + note + affiche\n"
							+ "• *blague* : blague aléatoire\n"
							+ "• *bonne blague* : la meilleure \n"
							+ "• *blague nulle* : la pire \n"
							+ "• *voir blague 3* : blague par id\n"
							+ "• *noter blague 3 note 8* : noter une blague\n"
							+ "• *supprimer blague 3* : supprimer\n"
							+ "• *titre blague <motclé>* : chercher une blague par titre\n"
							+ "• *ajouter blague titre | texte | note* : créer une blague\n"
							+ "• *modifier blague 3 | titre | texte | note* : modifier une blague existante\n");

		} catch (NumberFormatException e) {
			reply(update, "⚠ Format invalide. Exemple : *noter blague 3 note 7*");
		} catch (Exception e) {
			reply(update, " Erreur : " + e.getMessage());
		}
	}

	private String formatJoke(Joke joke) {
		return " *" + joke.getTitle() + "* (id: " + joke.getId() + ")\n\n"
				+ joke.getText()
				+ "\n\n Note : " + joke.getRating() + "/10";
	}

	private String formatForecast(ForecastResponse forecast) {
		StringBuilder sb = new StringBuilder();
		sb.append("Prévisions météo - ").append(forecast.getVille()).append("*\n\n");

		List<ForecastDay> days = forecast.getForecast();
		if (days.isEmpty()) {
			return sb.append("Aucune prévision disponible.").toString();
		}

		for (ForecastDay day : days) {
			sb.append("*").append(day.getDate()).append("*\n");
			sb.append("🌡 Matin : ").append(day.getTemperatureMatin()).append("°C\n");
			sb.append("🌡 Après-midi : ").append(day.getTemperatureApresMidi()).append("°C\n");
			sb.append("☁ Conditions : ").append(day.getMeteo())
					.append(" ‒ ").append(day.getDetails()).append("\n\n");
		}

		return sb.toString();
	}

	private void reply(Update update, String text) {
		String chatId = String.valueOf(update.getMessage().getChat().getId());
		Integer replyTo = update.getMessage().getMessageId();
		telegramService.sendMessage(chatId, text, replyTo);
	}
	private void replyPhoto(Update update, String photoUrl, String caption) {
		String chatId  = String.valueOf(update.getMessage().getChat().getId());
		Integer replyTo = update.getMessage().getMessageId();
		telegramService.sendPhoto(chatId, photoUrl, caption, replyTo);
	}
}