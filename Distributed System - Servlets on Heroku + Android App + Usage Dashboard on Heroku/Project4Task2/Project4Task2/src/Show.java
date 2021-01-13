import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Show {
    // reusable JSON parser
    static JSONParser jsonParser = new JSONParser();

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

    /**
     * This method is designed to handle null values
     * due to any missing field in the received output
     * @param fieldValue field String to process
     * @return processed field
     */
    static String treatNull(String fieldValue) {
        if (fieldValue == null) {
            return "N/A";
        }
        return fieldValue;
    }

    // Default constructor to create an empty show
    public Show() {

    }

    // Constructor to create Show object with information present
    public Show(String id, String name, String type, String language, String genresJSON, String status, String runtime,
                String premiered, String ratingJSON, String imageJSON, String summary) {
        // Store these fields after checking for any nulls
        // nulls will be replaced with "N/A" string for user-friendliness
        this.id = treatNull(id);
        this.name = treatNull(name);
        this.type = treatNull(type);
        this.language = treatNull(language);
        this.status = treatNull(status);
        this.runtime = treatNull(runtime);
        this.premiered = treatNull(premiered);
        this.summary = treatNull(summary).replaceAll("\\<.*?\\>", ""); // Remove any HTML

        // Genres is a JSON array
        JSONArray genresArray;
        this.genres = "";
        try {
            // Try processing JSON array
            genresArray = (JSONArray) jsonParser.parse(genresJSON);
            // If genres present
            if (genresArray != null && genresArray.size() != 0) {
                int i = 0;
                // Fetch each genre and store it in a nicely formatted String
                for (Object g : genresArray) {
                    if (i > 0) {
                        this.genres += ", ";
                    }
                    this.genres += g.toString();
                    i++;
                }
            } else {
                // No genre available
                this.genres = "N/A";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Ratings is a JSON string
        JSONObject parsedRating;
        try {
            // Try processing JSON
            parsedRating = (JSONObject) jsonParser.parse(ratingJSON);
            if (parsedRating != null) {
                // If not null, store it
                this.rating = parsedRating.get("average").toString();
            } else {
                // If null, store as "N/A"
                this.rating = "N/A";
            }
        } catch (ParseException e) {
            // Handling error
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Handling error
            this.rating = "N/A";
        }

        // Image URL has to be fetched from json string
        JSONObject parsedImage;
        try {
            // Try processing the JSON string
            parsedImage = (JSONObject) jsonParser.parse(imageJSON);
            if (parsedImage != null) {
                // If image present, return it
                this.image = parsedImage.get("medium").toString();
            } else {
                // If not, return a default image URL
                this.image = "http://lh3.ggpht.com/cr6L4oleXlecZQBbM1EfxtGggxpRK0Q1cQ8JBtLjJdeUrqDnXAeBHU30trRRnMUFfSo=w300";
            }
        } catch (ParseException e) {
            // Handling error
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Handling error
            this.image = "http://lh3.ggpht.com/cr6L4oleXlecZQBbM1EfxtGggxpRK0Q1cQ8JBtLjJdeUrqDnXAeBHU30trRRnMUFfSo=w300";
        }
    }

    /**
     * This method overwrites the default toString() methos
     * to return stored show details in JSON format
     * @return JSON representation of the TV show
     */
    @Override
    public String toString() {
        if (id == null) {
            // If empty show
            return null;
        }

        // Else, return show details in JSON format
        return "{"
                + "\"id\":\"" + id.replace("\"", "") + "\","
                + "\"name\":\"" + name.replace("\"", "") + "\","
                + "\"type\":\"" + type.replace("\"", "") + "\","
                + "\"language\":\"" + language.replace("\"", "") + "\","
                + "\"genres\":\"" + genres.replace("\"", "") + "\","
                + "\"status\":\"" + status.replace("\"", "") + "\","
                + "\"runtime\":\"" + runtime.replace("\"", "") + "\","
                + "\"premiered\":\"" + premiered.replace("\"", "") + "\","
                + "\"rating\":\"" + rating.replace("\"", "") + "\","
                + "\"image\":\"" + image.replace("\"", "") + "\","
                + "\"summary\":\"" + summary.replace("\"", "") + "\""
                + "}";
    }
}
