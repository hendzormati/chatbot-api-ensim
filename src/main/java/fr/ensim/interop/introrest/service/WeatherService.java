package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.model.City;
import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.model.OpenWeather;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${open.weather.api.token}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Meteo getMeteoByCity(String nomVille) {
        ResponseEntity<City[]> response = restTemplate.getForEntity(
                "http://api.openweathermap.org/geo/1.0/direct?q={ville}&limit=1&appid={key}",
                City[].class, nomVille, apiKey
        );

        City[] cities = response.getBody();
        if (cities == null || cities.length == 0) {
            throw new RuntimeException("Ville introuvable : " + nomVille);
        }
        City city = cities[0];
        OpenWeather openWeather = restTemplate.getForObject(
                "http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang=fr&appid={key}",
                OpenWeather.class, city.getLat(), city.getLon(), apiKey
        );
        Meteo meteo = new Meteo();
        meteo.setMeteo(openWeather.getWeather().get(0).getMain());
        meteo.setDetails(openWeather.getWeather().get(0).getDescription());
        meteo.setTemperature(openWeather.getMain().getTemp());
        return meteo;
    }

    public String formatForTelegram(String ville, Meteo meteo) {
        return "🌤 *Météo à " + ville + "*\n\n"
                + "🌡 Température : " + meteo.getTemperature() + "°C\n"
                + "☁ Conditions : " + meteo.getMeteo() + "\n"
                + " Détails : " + meteo.getDetails();
    }
}