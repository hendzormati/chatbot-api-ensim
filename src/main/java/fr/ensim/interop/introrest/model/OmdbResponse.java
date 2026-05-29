package fr.ensim.interop.introrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OmdbResponse {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Response")
    private String response; // "True" ou "False"

    @JsonProperty("Error")
    private String error;

    public boolean isFound() {
        return "True".equalsIgnoreCase(response);
    }

    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getPlot() { return plot; }
    public String getImdbRating() { return imdbRating; }
    public String getPoster() { return poster; }
    public String getImdbID() { return imdbID; }
    public String getError() { return error; }
}