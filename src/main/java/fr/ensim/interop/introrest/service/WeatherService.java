package fr.ensim.interop.introrest.service;

import fr.ensim.interop.introrest.exception.ExternalServiceException;
import fr.ensim.interop.introrest.exception.NotFoundException;
import fr.ensim.interop.introrest.model.City;
import fr.ensim.interop.introrest.model.ForecastItem;
import fr.ensim.interop.introrest.model.ForecastOpenWeather;
import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.model.OpenWeather;
import fr.ensim.interop.introrest.model.generated.ForecastDay;
import fr.ensim.interop.introrest.model.generated.ForecastResponse;
import fr.ensim.interop.introrest.model.generated.MeteoResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                City[].class, nomVille, apiKey);

        City[] cities = response.getBody();
        if (cities == null || cities.length == 0) {
            throw new NotFoundException("Ville introuvable : " + nomVille);
        }
        City city = cities[0];
        OpenWeather openWeather = restTemplate.getForObject(
                "http://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang=fr&appid={key}",
                OpenWeather.class, city.getLat(), city.getLon(), apiKey);
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

    public ForecastResponse getForecast(String nomVille) {
        ResponseEntity<City[]> response = restTemplate.getForEntity(
                "http://api.openweathermap.org/geo/1.0/direct?q={ville}&limit=1&appid={key}",
                City[].class, nomVille, apiKey);

        City[] cities = response.getBody();
        if (cities == null || cities.length == 0) {
            throw new NotFoundException("Ville introuvable : " + nomVille);
        }
        City city = cities[0];

        ForecastOpenWeather forecastData = restTemplate.getForObject(
                "http://api.openweathermap.org/data/2.5/forecast?lat={lat}&lon={lon}&units=metric&lang=fr&appid={key}",
                ForecastOpenWeather.class,
                city.getLat(), city.getLon(), apiKey);

        if (forecastData == null || forecastData.getList() == null) {
            throw new ExternalServiceException("Impossible de récupérer les prévisions météo");
        }

        MeteoResponse today = toMeteoResponse(getMeteoByCity(nomVille), nomVille);

        List<ForecastDay> forecast = buildForecastDays(forecastData.getList());

        return new ForecastResponse()
                .ville(nomVille)
                .today(today)
                .forecast(forecast);
    }

    private MeteoResponse toMeteoResponse(Meteo meteo, String ville) {
        return new MeteoResponse()
                .ville(ville)
                .meteo(meteo.getMeteo())
                .details(meteo.getDetails())
                .temperature(meteo.getTemperature());
    }

    private List<ForecastDay> buildForecastDays(List<ForecastItem> items) {
        Map<LocalDate, ForecastDay> map = new LinkedHashMap<>();

        for (ForecastItem item : items) {
            if (item.getDtTxt() == null || item.getWeather() == null || item.getWeather().isEmpty()) {
                continue;
            }

            LocalDate date = LocalDate.parse(item.getDtTxt().substring(0, 10));
            if (!date.isAfter(LocalDate.now())) {
                continue;
            }
            if (map.size() >= 2 && !map.containsKey(date)) {
                continue;
            }

            ForecastDay day = map.computeIfAbsent(date, d -> new ForecastDay().date(d));

            String hour = item.getDtTxt().substring(11, 13);
            if ("09".equals(hour) || "06".equals(hour)) {
                day.setTemperatureMatin(item.getMain().getTemp());
            }
            if ("12".equals(hour) || "15".equals(hour)) {
                day.setTemperatureApresMidi(item.getMain().getTemp());
            }
            if (day.getMeteo() == null) {
                day.setMeteo(item.getWeather().get(0).getMain());
                day.setDetails(item.getWeather().get(0).getDescription());
            }
        }

        return new ArrayList<>(map.values());
    }
}