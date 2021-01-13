import java.util.ArrayList;
import java.util.List;

public class LogItem {
    // Global log entry count
    static int count;

    // Log item details
    int id;
    String manufacturer;
    String model;
    String os;
    String searchString;
    String foundShow;
    long requestProcessingTime;
    String timeStamp;
    String requestSuccessful;

    public LogItem() {
        // Just increment the count in the default constructor
        id = ++count;
    }

    public LogItem(String manufacturer,
                   String model,
                   String os,
                   String searchString,
                   String foundShow,
                   String requestProcessingTime,
                   String timeStamp,
                   String requestSuccessful) {
        // Increment the global count
        id = ++count;
        // Store read log details
        this.manufacturer = manufacturer;
        this.model = model;
        this.os = os;
        // Handle empty/invalid search strings
        if (searchString == null || searchString.equals("")) {
            // null string
            this.searchString = "<em>&laquo;detected invalid&raquo;</em>";
            this.foundShow = "<em>&laquo;not searched&raquo;</em>";
        } else if (foundShow == null || foundShow.equals("")) {
            // string not null, but no TV show found
            this.searchString = searchString.toLowerCase();
            this.foundShow = "<em>&laquo;no result&raquo;</em>";
        } else {
            // everything ok
            this.searchString = searchString.toLowerCase();
            this.foundShow = foundShow;
        }

        this.requestProcessingTime = Long.parseLong(requestProcessingTime);
        this.timeStamp = timeStamp;
        this.requestSuccessful = Boolean.parseBoolean(requestSuccessful) ? "Yes" : "No";

        // Build TV show statistic list
        if (foundShow != null && !foundShow.equals("")) {
            ShowEntry currentShow = new ShowEntry(foundShow);
            if (DashboardModel.foundShows.contains(currentShow)) {
                // If show present, find it and increment its counter
                for (ShowEntry se : DashboardModel.foundShows) {
                    if (se.equals(currentShow)) se.incrementCount();
                }
            } else {
                // Else create a new entry
                DashboardModel.foundShows.add(currentShow);
            }
        }

        // Build search statistics list
        if (searchString != null && !searchString.equals("")) {
            SearchStringEntry currentSearch = new SearchStringEntry(searchString);
            currentSearch.setLatestShowResult(foundShow);
            if (DashboardModel.searchedStrings.contains(currentSearch)) {
                // If search keyword present, find it and increment its counter
                for (SearchStringEntry sse : DashboardModel.searchedStrings) {
                    if (sse.equals(currentSearch)) sse.incrementCount();
                }
            } else {
                // Else create a new entry
                DashboardModel.searchedStrings.add(currentSearch);
            }
        }
    }

    /**
     * This method returns the log entry in HTML table row format
     * @return
     */
    @Override
    public String toString() {
        return "<tr>" +
                "<td>" + id + "</td>" +
                "<td>" + manufacturer + "</td>" +
                "<td>" + model + "</td>" +
                "<td>" + os + "</td>" +
                "<td>" + timeStamp + "</td>" +
                "<td>" + searchString + "</td>" +
                "<td>" + foundShow + "</td>" +
                "<td>" + requestSuccessful + "</td>" +
                "<td>" + requestProcessingTime + " ms</td>" +
                "</tr>";
    }
}
