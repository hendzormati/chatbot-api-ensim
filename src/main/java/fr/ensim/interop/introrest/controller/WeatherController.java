package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/meteo")
    public ResponseEntity<?> getMeteo(@RequestParam("ville") String ville) {
        try {
            Meteo meteo = weatherService.getMeteoByCity(ville);
            return ResponseEntity.ok(meteo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}