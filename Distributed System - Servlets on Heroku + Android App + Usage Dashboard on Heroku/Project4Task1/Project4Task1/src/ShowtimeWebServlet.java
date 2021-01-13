import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ShowtimeWebServlet")
public class ShowtimeWebServlet extends HttpServlet {
    // For future use
    PrintWriter out;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        // Don't process POST requests
        out = response.getWriter();
        out.println("Call to post method not allowed");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        try {
            // Create ShowtimeAPIModel object to interact with 3rd party API
            ShowtimeAPIModel apiModel = new ShowtimeAPIModel();
            // Initialize PrintWriter out to write json output
            out = response.getWriter();
            // Get the search keyword
            String searchString = request.getParameter("searchString");
            // Fetch results from TVmaze API
            Show resultShow = apiModel.getShows(searchString);

            // Default output
            String outText = "Search string is null or no matching show found.";
            // If not null, write the actual result output
            if (searchString!= null && resultShow.id != null) {
                // resultShow.toString() returns the output in JSON format. Store it
                outText = resultShow.toString();
            }
            // Write output
            out.println(outText);
        } catch(Error e) {
            // Handle any error
            System.out.println("Error while searching show.");
        }
    }
}
