package edu.cmu.aandrask;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import android.annotation.SuppressLint;
import android.os.Build;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/*
 * This class provides capabilities to search for an image on Flickr.com given a search term.  The method "search" is the entry to the class.
 * Network operations cannot be done from the UI thread, therefore this class makes use of an AsyncTask inner class that will do the network
 * operations in a separate worker thread.  However, any UI updates should be done in the UI thread so avoid any synchronization problems.
 * onPostExecution runs in the UI thread, and it calls the ImageView pictureReady method to do the update.
 *
 */
public class GetShow {
    InterestingShow is = null;
    static JSONParser jsonParser = new JSONParser();

    /**
     * This method fetches device details from os.Build
     *
     * @return
     */
    static String[] getDeviceDetails() {
        // Get details
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String OSVersion = Build.VERSION.RELEASE;
        // Return as an array
        return new String[]{manufacturer, model, OSVersion};
    }


    /*
     * search is the public GetShow method.  Its arguments are the search term, and the InterestingShow object that called it.
     * This provides a callback path such that the showready method in that object is called when the show is read from the API.
     */
    public void search(String searchTerm, InterestingShow is) {
        this.is = is;
        new GetShow.AsyncShowSearch().execute(searchTerm);
    }

    // Method to encode a string to a URL-friendly format
    // Reference: https://docs.oracle.com/javase/7/docs/api/java/net/URLEncoder.html#encode(java.lang.String)
    static String encodeValue(String value) {
        try {
            // Encode special characters
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            // Error handling
            throw new RuntimeException(ex.getCause());
        }
    }

    /**
     * This method takes in the search keyword, sends it to our Heroku API,
     * receives output from it and returns the received json text
     * of found show
     *
     * @param showName keyword to search
     * @return String json of the show
     * @throws IOException
     */
    static String searchShow(String showName) {

        // Response string
        String responseJSON = "";
        // Setup connection
        HttpURLConnection conn;
        // HTTP status holder
        int status = 0;

        try {
            // Pass search keyword to the API along with device details
            URL url = new URL(
                    "https://animish-ds-project4task2.herokuapp.com/searchShow/?searchString=" + encodeValue(showName)
                            + "&reqFromShowtimeApp=true"
                            + "&manufacturer=" + encodeValue(getDeviceDetails()[0])
                            + "&model=" + encodeValue(getDeviceDetails()[1])
                            + "&os=" + encodeValue(getDeviceDetails()[2])
            );
            // Establish connection
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/json");

            // Wait for response
            status = conn.getResponseCode();

            // If things went poorly, don't try to read any response, just return.
            if (status != 200) {
                // not using msg
                String msg = conn.getResponseMessage();
                return "-1";
            }

            String output = "";
            // We received a valid response from the API
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            // Read each line
            while ((output = br.readLine()) != null) {
                responseJSON += output;
            }

            // Disconnect once done
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return response string
        return responseJSON;
    }

    /**
     * This method takes in show JSON and processes it to create a Show object
     * @param showText Show details in JSON format
     * @return Show object with show information
     */
    static Show createShowFromJSON(String showText) {
        // If null or error string
        if (showText == null || showText.equalsIgnoreCase("Search string is null or no matching show found.")) {
            return null;
        }
        // Parse JSON
        JSONObject showJSON = null;
        try {
            showJSON = (JSONObject) jsonParser.parse(showText);
            if (showJSON == null) return null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Create and return Show object
        return new Show(
                showJSON.get("id").toString(),
                showJSON.get("name").toString(),
                showJSON.get("type").toString(),
                showJSON.get("language").toString(),
                showJSON.get("genres").toString(),
                showJSON.get("status").toString(),
                showJSON.get("runtime").toString(),
                showJSON.get("premiered").toString(),
                showJSON.get("rating").toString(),
                showJSON.get("image").toString(),
                showJSON.get("summary").toString()
        );
    }

    /*
     * AsyncTask provides a simple way to use a thread separate from the UI thread in which to do network operations.
     * doInBackground is run in the helper thread.
     * onPostExecute is run in the UI thread, allowing for safe UI updates.
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncShowSearch extends AsyncTask<String, Void, Show> {
        protected Show doInBackground(String... urls) {
            return search(urls[0]);
        }

        protected void onPostExecute(Show show) {
            is.pictureReady(show);
        }

        /*
         * Search our Heroku API for the searchTerm argument, and return the Show object
         */
        private Show search(String searchTerm) {
            // Get the Show object from the API
            Show resultShow = createShowFromJSON(searchShow(searchTerm));
            if (resultShow == null || resultShow.id == null) {
                return null; // no show found
            }
            // Generate show image bitmap and store it in the same object
            try {
                URL u = new URL(resultShow.image);
                resultShow.imageBitmap = getRemoteImage(u);
                // Return Show
                return resultShow;
            } catch (Exception e) {
                e.printStackTrace();
                return null; // so compiler does not complain
            }
        }
    }

    /*
     * Given a URL referring to an image, return a bitmap of that image
     */
    private Bitmap getRemoteImage(final URL url) {
        try {
            final URLConnection conn = url.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            return bm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}