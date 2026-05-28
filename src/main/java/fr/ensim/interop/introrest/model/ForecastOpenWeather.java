package fr.ensim.interop.introrest.model;

import java.util.List;

public class ForecastOpenWeather {
    private List<ForecastItem> list;
    public List<ForecastItem> getList() { return list; }
    public void setList(List<ForecastItem> list) { this.list = list; }
}
