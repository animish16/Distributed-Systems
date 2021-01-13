import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

@WebServlet(name = "ShowtimeWebServlet")
public class ShowtimeWebServlet extends HttpServlet {
    // Objects for future use
    ShowtimeAPIModel apiModel = new ShowtimeAPIModel();
    DashboardModel dbModel;
    PrintWriter out;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        // Don't process POST requests
        out = response.getWriter();
        out.println("Call to post method not allowed");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        // Create DashBoardModel object
        dbModel = new DashboardModel();
        // Setup and connect to MongoDB
        dbModel.setupMongoDB();

        try {
            out = response.getWriter();
            String searchString = request.getParameter("searchString");

            // Note start time
            Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
            long start = System.currentTimeMillis();
            Show resultShow = apiModel.getShows(searchString);
            // Note end time
            long end = System.currentTimeMillis();
            long requestProcessingTime = (end - start);

            // Default output
            String outText = "Search string is null or no matching show found.";
            String foundShow = null;
            // If not null, write the actual result output
            if (searchString != null && resultShow.id != null) {
                outText = resultShow.toString();
                foundShow = resultShow.name;
            }

            // Set request successful flg
            boolean requestSuccessful = true;
            if (resultShow.id == null) {
                requestSuccessful = false;
            }

            // Fetch device details
            String reqFromShowtimeApp = request.getParameter("reqFromShowtimeApp");
            if (reqFromShowtimeApp != null) {
                String manufacturer = request.getParameter("manufacturer");
                String model = request.getParameter("model");
                String os = request.getParameter("os");

                // Store log
                dbModel.storeRequestLog(
                        manufacturer,
                        model,
                        os,
                        searchString,
                        foundShow,
                        requestProcessingTime,
                        timeStamp.toString(),
                        requestSuccessful
                );
            }

            out.println(outText);
        } catch (Error e) {
            // Handle errors
            System.out.println(e.fillInStackTrace());
            System.out.println("Error while searching show.");
        }
    }
}
