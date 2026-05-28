package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.api.MessageApi;
import fr.ensim.interop.introrest.model.generated.SendMessageRequest;
import fr.ensim.interop.introrest.model.generated.SendMessagesRequest;
import fr.ensim.interop.introrest.model.generated.MessageResponse;
import fr.ensim.interop.introrest.service.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageRestController implements MessageApi {

	@Autowired
	private TelegramService telegramService;

	@Override
	public ResponseEntity<MessageResponse> sendMessage(SendMessageRequest request) {
		telegramService.sendMessage(request.getChatId(), request.getText());
		telegramService.sendMessage(request.getChatId(),
				"Bonjour ! Je suis ton assistant. Envoie blague ou meteo [ville]* !");

		MessageResponse response = new MessageResponse();
		response.setSuccess(true);
		response.setChatId(request.getChatId());
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<String> sendMessages(SendMessagesRequest request) {
		for (String text : request.getTexts()) {
			telegramService.sendMessage(request.getChatId(), text);
		}
		return ResponseEntity.ok(request.getTexts().size() + " message(s) envoyé(s) !");
	}
}