package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.Joke;
import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.model.telegram.Update;
import fr.ensim.interop.introrest.service.JokeService;
import fr.ensim.interop.introrest.service.TelegramService;
import fr.ensim.interop.introrest.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
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
		// "meteo Paris" → "Paris", "météo le mans" → "le mans"
		String cleaned = text.replace("météo", "meteo").replace("meteo", "").trim();
		return cleaned.isEmpty() ? "Paris" : cleaned; // Paris par défaut
	}
	private void handleMessage(Update update) {
		String chatId = String.valueOf(update.getMessage().getChat().getId());
		String text   = update.getMessage().getText().toLowerCase().trim();

		try {
			if (text.startsWith("meteo") || text.startsWith("météo")) {
				String ville = extractCity(text);
				try {
					Meteo meteo = weatherService.getMeteoByCity(ville);
					telegramService.sendMessage(chatId, weatherService.formatForTelegram(ville, meteo));
				} catch (RuntimeException e) {
					telegramService.sendMessage(chatId, "❌ " + e.getMessage());
				}

			}
			else if (text.contains("blague") && (text.contains("nulle") || text.contains("mauvaise")|| text.contains("pire"))) {
				jokeService.getWorst().ifPresent(joke ->
						telegramService.sendMessage(chatId, formatJoke(joke))
				);
			} else if (text.contains("blague") && (text.contains("bonne") || text.contains("meilleure"))) {
				jokeService.getBest().ifPresent(joke ->
						telegramService.sendMessage(chatId, formatJoke(joke))
				);
			} else if (text.contains("blague") && (text.contains("voir")|| text.contains("afficher") || text.contains("montrer"))) {
				String[] parts = text.split("\\s+");
				long id = Long.parseLong(parts[parts.length - 1]);
				jokeService.getById(id)
						.ifPresentOrElse(
								joke -> telegramService.sendMessage(chatId, formatJoke(joke)),
								()   -> telegramService.sendMessage(chatId, "Blague #" + id + " introuvable.")
						);

			} else if (text.contains("blague") && (text.contains("supprimer")|| text.contains("effacer"))) {
				String[] parts = text.split("\\s+");
				long id = Long.parseLong(parts[parts.length - 1]);
				boolean deleted = jokeService.delete(id);
				telegramService.sendMessage(chatId,
						deleted ? " Blague #" + id + " supprimée." : "Blague #" + id + " introuvable.");

			} else if (text.startsWith("noter blague")) {
				// format attendu : "noter blague {id} note {note}"
				String[] parts = text.split("\\s+");
				long id        = Long.parseLong(parts[2]);
				double rating  = Double.parseDouble(parts[parts.length - 1]);
				jokeService.rate(id, rating)
						.ifPresentOrElse(
								joke -> telegramService.sendMessage(chatId,
										" Blague *" + joke.getTitle() + "* notée " + rating + "/10"),
								()   -> telegramService.sendMessage(chatId, " Blague #" + id + " introuvable.")
						);

			} else if (text.contains("blague")) {
				telegramService.sendMessage(chatId, formatJoke(jokeService.getRandom()));
			} else if (text.equals("/start")) {
				telegramService.sendMessage(chatId,
						"👋 Bonjour ! Voici ce que je sais faire :\n\n"
								+ "• *meteo Paris* — météo d'une ville 🌤\n"
								+ "• *blague* — blague aléatoire\n"
								+ "• *bonne blague* — la meilleure 😂\n"
								+ "• *blague nulle* — la pire 😐\n"
								+ "• *voir blague 3* — blague par id\n"
								+ "• *noter blague 3 note 8* — noter une blague\n"
								+ "• *supprimer blague 3* — supprimer\n");
			}

		} catch (NumberFormatException e) {
			telegramService.sendMessage(chatId, "⚠ Format invalide. Exemple : *noter blague 3 note 7*");
		} catch (Exception e) {
			telegramService.sendMessage(chatId, " Erreur : " + e.getMessage());
		}
	}

	private String formatJoke(Joke joke) {
		return " *" + joke.getTitle() + "* (id: " + joke.getId() + ")\n\n"
				+ joke.getText()
				+ "\n\n Note : " + joke.getRating() + "/10";
	}

}