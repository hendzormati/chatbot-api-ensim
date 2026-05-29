package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.api.MtoApi;
import fr.ensim.interop.introrest.model.Meteo;
import fr.ensim.interop.introrest.model.generated.MeteoResponse;
import fr.ensim.interop.introrest.model.generated.ForecastResponse;
import fr.ensim.interop.introrest.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController implements MtoApi {

    @Autowired
    private WeatherService weatherService;

    @Override
    public ResponseEntity<MeteoResponse> getMeteo(String ville) {
        Meteo meteo = weatherService.getMeteoByCity(ville);
        MeteoResponse response = new MeteoResponse()
                .ville(ville)
                .meteo(meteo.getMeteo())
                .details(meteo.getDetails())
                .temperature(meteo.getTemperature());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ForecastResponse> getMeteoForecast(String ville) {
        ForecastResponse forecast = weatherService.getForecast(ville);
        return ResponseEntity.ok(forecast);
    }
}