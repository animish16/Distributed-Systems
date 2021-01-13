import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ShowtimeAPIModel {
    // reusable JSONParser object
    static JSONParser jsonParser = new JSONParser();

    /**
     * This method is designed to handle null pointer errors
     * due to any missing field in the API output
     * @param showJSON JSONObject with output from API
     * @param field field to be processed
     * @return processed field in String format, or null if error
     */
    static String processJSONField(JSONObject showJSON, String field) {
        try { // Give fetching information from the JSON a try
            return showJSON.get(field).toString();
        } catch (NullPointerException n) { // Return null if the information is not present
            return null;
        }
    }

    /**
     * This method takes in the search keyword, sends it to TVmaze 3rd party API,
     * receives output from it and returns a processed Show object with the details
     * of found show
     * @param searchKeyword keyword to search on TVmaze catalog
     * @return Show object with found show's details. All fields are empty if show not found
     * @throws IOException
     */
    Show getShows(String searchKeyword) throws IOException {
        // Response string
        String response = "";
        // Setup connection
        HttpURLConnection conn;
        // HTTP status holder
        int status;

        try {
            // Pass search keyword to the API
            URL url = new URL("https://api.tvmaze.com/singlesearch/shows?q=" + searchKeyword);
            // Establish connection
            conn = (HttpURLConnection) url.openConnection();
            // Set request method as GET and text/json as intended output from the API
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/json");

            // Wait for response
            status = conn.getResponseCode();

            // If things went poorly, don't try to read any response, just return an empty show
            if (status != 200) {
                return new Show();
            }

            // We received a valid response from the API
            String output;
            // Things went well so let's read the response
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            // Read each line
            while ((output = br.readLine()) != null) {
                response += output;

            }

            // Disconnect once done
            conn.disconnect();

        } catch (ProtocolException | MalformedURLException e) {
            // Handling any error
            e.printStackTrace();
        } catch (IOException e) {
            // Handling any error
            e.printStackTrace();
        }

        // Process received JSON output from the API
        JSONObject showJSON = null;
        try {
            // Give it a try
            showJSON = (JSONObject) jsonParser.parse(response);
            // Show not found on TVmaze
            if (showJSON == null) return null;
        } catch (ParseException e) {
            // Handling any error
            e.printStackTrace();
        }

        // Show found. Create a new Show object with received details and return it
        return new Show(
                processJSONField(showJSON, "id"),
                processJSONField(showJSON, "name"),
                processJSONField(showJSON, "type"),
                processJSONField(showJSON, "language"),
                processJSONField(showJSON, "genres"),
                processJSONField(showJSON, "status"),
                processJSONField(showJSON, "runtime"),
                processJSONField(showJSON, "premiered"),
                processJSONField(showJSON, "rating"),
                processJSONField(showJSON, "image"),
                processJSONField(showJSON, "summary")
        );
    }
}
