package edu.cmu.aandrask;

import android.graphics.Bitmap;

public class Show {
    // Variables to hold show details
    String id;
    String name;
    String type;
    String language;
    String genres;
    String status;
    String runtime;
    String premiered;
    String rating;
    String image;
    String summary;
    Bitmap imageBitmap;

    // Default constructor to create an empty show
    public Show() {

    }

    // Constructor to create Show object with information present
    public Show(String id, String name, String type, String language, String genres, String status, String runtime,
                String premiered, String rating, String image, String summary) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.language = language;
        this.genres = genres;
        this.status = status;
        this.runtime = runtime;
        this.premiered = premiered;
        this.image = image;
        this.summary = summary.replaceAll("\\<.*?\\>", ""); // Remove any HTML
        this.rating = rating;
    }

    @Override
    public String toString() {
        if (id == null) {
            // Empty show
            return "Empty show details";
        }

        // Nicely formatted show JSON representation
        return "{"
                + "\"id\":\"" + id + "\","
                + "\"name\":\"" + name + "\","
                + "\"type\":\"" + type + "\","
                + "\"language\":\"" + language + "\","
                + "\"genres\":\"" + genres + "\","
                + "\"status\":\"" + status + "\","
                + "\"runtime\":\"" + runtime + "\","
                + "\"premiered\":\"" + premiered + "\","
                + "\"rating\":\"" + rating + "\","
                + "\"image\":\"" + image + "\","
                + "\"summary\":\"" + summary + "\""
                + "}";
    }
}
