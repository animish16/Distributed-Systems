import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardModel {
    // For future use
    static MongoDatabase database;
    static MongoCollection<Document> collection;
    static MongoClient mongoClient;
    static JSONParser jsonParser = new JSONParser();

    List<LogItem> logItemList; // holds logs read from mongodb
    static List<ShowEntry> foundShows; // holds show counts
    static List<SearchStringEntry> searchedStrings; // holds search keyword counts and related TV show

    /**
     * This method establishes connection with MongoDB database
     * Code referred from:
     * http://mongodb.github.io/mongo-java-driver/3.11/driver/getting-started/quick-start/
     */
    void setupMongoDB() {
        if (database == null) {
            // Turn verbose output off
            Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
            // Setup connection with the database
            MongoClientURI uri = new MongoClientURI(
                    "mongodb+srv://dbUser:dbUserAnimish@cluster0-c8yfj.mongodb.net/test?retryWrites=true&w=majority"
            );
            // Create mongodb client
            mongoClient = new MongoClient(uri);
            // Access the database in which our desired collection is stored
            database = mongoClient.getDatabase("ShowtimeLogs");
            // Access collection in which we are pushing the logs
            collection = database.getCollection("ShowtimeLogs");
            // If we reach here, that means the connection was successful
            // Initialize the ArrayLists to be used below for the dashboard
            foundShows = new ArrayList<>();
            searchedStrings = new ArrayList<>();
        }
    }

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
     * This method is called by ShowtimeAPIServlet to store request information
     * @param manufacturer Manufacturer of the device from which the request is received
     * @param model Model of the device
     * @param os Anroid OS version of the device
     * @param searchString Requested search keyword
     * @param foundShow Show name matched for the search keyword
     * @param requestProcessingTime Request process time from TVmaze API
     * @param timeStamp Request timestamp
     * @param requestSuccessful Was the request successful?
     */
    void storeRequestLog(String manufacturer,
                         String model,
                         String os,
                         String searchString,
                         String foundShow,
                         long requestProcessingTime,
                         String timeStamp,
                         boolean requestSuccessful) {
        // Handle null search strings
        if (searchString == null) searchString = " <null>";
        // Create a Java bson document containing the information to be stored
        Document doc = new Document("manufacturer", manufacturer)
                .append("model", model)
                .append("os", os)
                .append("searchString", searchString)
                .append("foundShow", foundShow)
                .append("requestProcessingTime", requestProcessingTime)
                .append("timeStamp", timeStamp)
                .append("requestSuccessful", requestSuccessful);
        // {ush the doc to the collection
        collection.insertOne(doc);
    }

    /**
     * This method is called by the DashboardServlet to access all the information
     * stored in the Mongo database
     */
    void readRequestLog() {
        // Create cursor (iterator)
        MongoCursor<Document> cursor = collection.find().iterator();
        // Initialize the ArrayList which will hold each log entry
        logItemList = new ArrayList<>();
        try {
            // Set count to 0
            LogItem.count = 0;
            // Create a JSON object to process the JSON information retrieved
            JSONObject fetchedUserString;
            // While the iterator has information, keep pulling it
            while (cursor.hasNext()) {
                // This LogItem object holds information about each log entry
                LogItem readLogEntry;
                // Try processing the log entry JSON
                // No field in the entry can be null
                fetchedUserString = (JSONObject) jsonParser.parse(cursor.next().toJson());
                // Create a new LogItem object and store log details in it
                readLogEntry = new LogItem(
                        processJSONField(fetchedUserString, "manufacturer"),
                        processJSONField(fetchedUserString, "model"),
                        processJSONField(fetchedUserString, "os"),
                        processJSONField(fetchedUserString, "searchString"),
                        processJSONField(fetchedUserString, "foundShow"),
                        processJSONField(
                                // requestProcessingTime is a JSON string, hence, needs additional processing
                                (JSONObject) jsonParser.parse(
                                        processJSONField(fetchedUserString, "requestProcessingTime")
                                ),
                                "$numberLong"),
                        processJSONField(fetchedUserString, "timeStamp"),
                        processJSONField(fetchedUserString, "requestSuccessful")
                );
                // Add the LogItem to the log ArrayList
                logItemList.add(readLogEntry);
            }
        } catch (ParseException e) {
            // Handling error
            e.printStackTrace();
        } finally {
            // Handling error
            cursor.close();
        }
    }

    /**
     * This method processes each log entry
     * and creates an HTML table row for each entry
     * @return String containing the whole log information
     */
    String createLogString() {
        String outString = "";

        // If no log rows present
        if (logItemList.size() == 0 || logItemList == null) {
            outString += "No logs recorded yet." +
                    "Logs need to be generated by running and using the mobile app created for this project.";
        } else {
            // Read each log entry in the ArrayList,
            // use its overwritten toString method to het the HTML table row
            for (LogItem log : logItemList) {
                outString += log.toString();
            }
        }
        // Return all the rows concatenated with each other
        return outString;
    }

    /**
     * This method creates request statistics like
     * how many requests received so far, how many were succcessful
     * and how many were not
     * @return
     */
    String createRequestStats() {
        // Total requests received
        int totalRequests = logItemList.size();
        // Successful requests to 0 for now
        int successfulRequests = 0;
        // If no log entries are stored yet
        if (logItemList.size() == 0 || logItemList == null) {
            return "<tr>" +
                    "<td>None</td>" +
                    "<td>None</td>" +
                    "<td>None</td>" +
                    "</tr>";
        } else {
            // Traverse each log row to find out if it was successful or not
            for (LogItem log : logItemList) {
                if (log.requestSuccessful.equalsIgnoreCase("yes")) successfulRequests++;
            }
            // Return HTML table row
            return "<tr>" +
                    "<td>" + totalRequests + "</td>" +
                    "<td>" + successfulRequests + "</td>" +
                    "<td>"+ (totalRequests - successfulRequests) + "</td>" +
                    "</tr>";
        }
    }

    /**
     * This method returns the average TVmaze request processing time
     * @return String average process time in ms
     */
    String avgReqProcessTime() {
        long totalReqProcessTime = 0;
        // If no entries, return 0
        if (logItemList.size() == 0 || logItemList == null) {
            return "0";
        } else {
            // Find total request time
            for (LogItem log : logItemList) {
                totalReqProcessTime += log.requestProcessingTime;
            }
        }
        // Return the average
        return String.valueOf(totalReqProcessTime / logItemList.size());
    }

    /**
     * This method returns the top 10 shows produced as result
     * with their request count
     * @return
     */
    String getTopTenShows() {
        String outString = "";
        // Sort shows stored in the list
        Collections.sort(foundShows);

        int rank = 0;
        int count = 0;
        // Traverse each show entry and return the statistics as an HTML table row
        for (ShowEntry show : foundShows) {
            outString += "<tr>" +
                    "<td>" + (++rank) + "</td>" +
                    "<td>" + show.showName + "</td>" +
                    "<td>" + show.count + "</td>" +
                    "</tr>";
            if ((++count) >= 10) {
                // Stop when 10
                break;
            }
        }
        return outString;
    }

    /**
     * This method returns the top 10 search keywords requested by app users
     * with their request count and the most recent result TV show
     * @return
     */
    String getTopTenSearchStrings() {
        String outString = "";
        Collections.sort(searchedStrings);

        int rank = 0;
        int count = 0;
        Collections.sort(searchedStrings);

        // Traverse each search keyword entry and return the statistics as an HTML table row
        for (SearchStringEntry key : searchedStrings) {
            outString += "<tr>" +
                    "<td>" + (++rank) + "</td>" +
                    "<td>" + key.searchString + "</td>" +
                    "<td>" + key.latestShowResult + "</td>" +
                    "<td>" + key.count + "</td>" +
                    "</tr>";
            if ((++count) >= 10) {
                // Stop when 10
                break;
            }
        }
        return outString;
    }
}
