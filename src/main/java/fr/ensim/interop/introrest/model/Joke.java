package fr.ensim.interop.introrest.model;

public class Joke {
    private Long id;
    private String title;
    private String text;
    private double rating;

    public Joke(Long id, String title, String text, double rating) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}