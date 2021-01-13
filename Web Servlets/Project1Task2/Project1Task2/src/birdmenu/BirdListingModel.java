package birdmenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class BirdListingModel {
    // Variable declaration
    static final int FETCH_LIMIT = 20;
    static final int MENU_SIZE = 10;
    static List<String> allBirds = Arrays.asList(AllBirds.allBirds);
    String fetchedContent = "";
    String[] menuOptions;

    public static String[] getBirds(String keyword) {
        /*** This function takes in a search keyword and returns a randomly generated list of bird names of length MENU_SIZE ***/
        String[] birdList; // to be returned
        List<String> searchResults = new ArrayList<>();

        // Get all matching bird names
        for (String bird : allBirds) {
            if (bird.toLowerCase().contains(keyword.toLowerCase())) {
                searchResults.add(bird);
            }
        }

        // Shuffle and fetch some names upto MENU_SIZE length
        Collections.shuffle(searchResults);

        // If number of results > MENU_SIZE, list size is MENU_SIZE otherwise, it is the number of results found
        int listSize = Math.min(searchResults.size(), MENU_SIZE);
        // Initiate the list
        birdList = new String[listSize];
        // Form and return the final bird list
        for(int i = 0; i < listSize; i++) {
            birdList[i] = searchResults.get(i);
        }
        return birdList;
    }

    private String fetch(String urlString) {
        /*** This function takes in a URL and returns its raw HTML content ***/
        String response = "";
        try {
            URL url = new URL(urlString);
            /*
             * Create an HttpURLConnection.  This is useful for setting headers
             * and for getting the path of the resource that is returned (which
             * may be different than the URL above if redirected).
             * HttpsURLConnection (with an "s") can be used if required by the site.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Eeek, an exception");
            // Do something reasonable.  This is left for students to do.
        }
        return response;
    }

    String[] scrapeBird(String chosenBird) throws UnsupportedEncodingException {
        /*** This method takes in bird name selected by user and returns a bird image link and its photographer's name ***/
        String response = "";

        // Form the full URL
        String fetchURL = "https://nationalzoo.si.edu/scbi/migratorybirds/featured_photo/bird.cfm?pix=" + chosenBird;

        // Get the HTML content
        response = fetch(fetchURL);

        // If connection failed, return {"0", "0"}, which will be detected by the controller
        if(response.equalsIgnoreCase("") || response.length() == 0) {
            return new String[] {"0", "0"};
        }

        // List to store bird image links
        List<String[]> fetchedImages = new ArrayList<>();

        boolean doneAll = false;
        int lastFound = 0;
        int found = 0;
        // Search key for the image
        String toFind = "https://nationalzoo.si.edu/scbi/migratorybirds/featured_photo/images/bigpic/";
        // Search key for the photographer's name
        String toFindAuth = "&copy; ";

        while (!doneAll) {
            /*** Get upto FETCH_LIMIT number of bird images from the bird result page ***/
            // Find the start of bird link
            int cutLeft = response.indexOf(toFind, lastFound);
            // If nothing found, exit loop
            if(cutLeft == -1) {
                doneAll = true;
                break;
            }
            // Find the end of bird image URL
            int cutRight = response.indexOf("\" alt=\"[Photo]\"", cutLeft);

            // Do the same for photographer's name
            int cutLeftAuth = response.indexOf(toFindAuth, cutLeft) + toFindAuth.length();
            int cutRightAuth = response.indexOf("</a>", cutLeftAuth);

            // Fetch URL and photographer's name
            String picURL = response.substring(cutLeft, cutRight);
            String authName = response.substring(cutLeftAuth, cutRightAuth);

            // Store in the list of String arrays
            String[] imgInfo = new String[] {picURL, authName};
            fetchedImages.add(imgInfo);

            // Update counters
            lastFound = cutRightAuth;
            found ++;
            // If limit reached, stop.
            if(found >= 20) {
                doneAll = true;
            }
        }

        // No images found
        if (found ==0) {
            return null;
        }

        // Return one image randomly
        return fetchedImages.get((new Random()).nextInt(Math.min(FETCH_LIMIT, fetchedImages.size())));
    }
}
