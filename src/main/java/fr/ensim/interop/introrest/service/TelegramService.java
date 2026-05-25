package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

        restTemplate.postForObject(url, body, Map.class);
    }
    private String getBotUrl() {
        return baseUrl + botToken;
    }
    public List<Update> getUpdates(long offset) {
        String url = getBotUrl() + "/getUpdates?offset=" + offset;
        ApiResponseUpdateTelegram response = restTemplate.getForObject(url, ApiResponseUpdateTelegram.class);
        if (response == null || !response.getOk()) return List.of();
        return response.getResult();
    }
}