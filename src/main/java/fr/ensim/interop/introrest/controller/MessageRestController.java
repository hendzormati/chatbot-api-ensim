package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.service.JokeService;
import fr.ensim.interop.introrest.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MessageRestController {

	@Autowired
	private TelegramService telegramService;

	@PostMapping("/message")
	public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> body) {
		String chatId = body.get("chatId");
		String text   = body.get("text");

		telegramService.sendMessage(chatId, text);
		telegramService.sendMessage(chatId, "Bonjour ! Je suis ton assistant. Comment puis-je t'aider ?");

		return ResponseEntity.ok("Message envoyé !");
	}
	@PostMapping("/messages")
	public ResponseEntity<String> sendMessages(@RequestBody Map<String, Object> body) {
		String chatId = (String) body.get("chatId");
		List<String> texts = (List<String>) body.get("texts");
		for (String text : texts) {
			telegramService.sendMessage(chatId, text);
		}
		return ResponseEntity.ok(texts.size() + " message(s) envoyé(s) !");
	}
}