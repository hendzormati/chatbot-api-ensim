package fr.ensim.interop.introrest.model;

public class Meteo {
    private String meteo;
    private String details;
    private Double temperature;

    public Meteo() {}
    public String getMeteo() { return meteo; }
    public void setMeteo(String meteo) { this.meteo = meteo; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}