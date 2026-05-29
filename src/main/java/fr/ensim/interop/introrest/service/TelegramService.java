package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.exception.BadRequestException;
import fr.ensim.interop.introrest.exception.ExternalServiceException;
import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.api.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String chatId, String text) {
        String url = baseUrl + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);

        postTelegramMessage(url, body);
    }

    public void sendMessage(String chatId, String text, Integer replyToMessageId) {
        String url = baseUrl + botToken + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        if (replyToMessageId != null) {
            body.put("reply_to_message_id", replyToMessageId);
        }

        postTelegramMessage(url, body);
    }

    private void postTelegramMessage(String url, Map<String, Object> body) {
        try {
            restTemplate.postForObject(url, body, Map.class);
        } catch (HttpClientErrorException ex) {
            String responseBody = ex.getResponseBodyAsString();
            String message = "Erreur Telegram : "
                    + (responseBody != null && !responseBody.isBlank() ? responseBody : ex.getStatusText());
            throw new BadRequestException(message);
        } catch (HttpServerErrorException ex) {
            String responseBody = ex.getResponseBodyAsString();
            String message = "Erreur serveur Telegram : "
                    + (responseBody != null && !responseBody.isBlank() ? responseBody : ex.getStatusText());
            throw new ExternalServiceException(message);
        } catch (ResourceAccessException ex) {
            throw new ExternalServiceException("Impossible de joindre l'API Telegram : " + ex.getMessage());
        } catch (Exception ex) {
            throw new ExternalServiceException("Erreur lors de l'envoi du message Telegram : " + ex.getMessage());
        }
    }

    public void sendPhoto(String chatId, String photoUrl, String caption, Integer replyTo) {
        String url = getBotUrl() + "/sendPhoto";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("photo", photoUrl);
        body.put("caption", caption);
        body.put("parse_mode", "Markdown");
        if (replyTo != null) {
            body.put("reply_to_message_id", replyTo);
        }

        restTemplate.postForObject(url, body, Map.class);
    }
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
    private String getBotUrl() {
        return baseUrl + botToken;
    }

    public List<Update> getUpdates(long offset) {
        String url = getBotUrl() + "/getUpdates?offset=" + offset;
        ApiResponseUpdateTelegram response = restTemplate.getForObject(url, ApiResponseUpdateTelegram.class);
        if (response == null || !response.getOk())
            return List.of();
        return response.getResult();
    }
}