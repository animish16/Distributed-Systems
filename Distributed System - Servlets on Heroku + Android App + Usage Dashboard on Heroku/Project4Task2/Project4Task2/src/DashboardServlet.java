import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DashboardServlet")
public class DashboardServlet extends HttpServlet {
    // For future use
    PrintWriter out;
    DashboardModel dbModel = new DashboardModel();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Don't process POST requests
        out = response.getWriter();
        out.println("Call to post method not allowed");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        out = response.getWriter();
        // Create DashboardModel object nd setup connection
        dbModel = new DashboardModel();
        dbModel.setupMongoDB();

        // Read logs from the mongodb collection
        dbModel.readRequestLog();
        // Generate and store the useful statistics to be passed to the JSP view
        request.setAttribute("requestStats", dbModel.createRequestStats());
        request.setAttribute("avgReqProcessTime", dbModel.avgReqProcessTime());
        request.setAttribute("logEntries", dbModel.createLogString());
        request.setAttribute("topTenShows", dbModel.getTopTenShows());
        request.setAttribute("topTenSearchStrings", dbModel.getTopTenSearchStrings());

        // Dispatch to the dashboard JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}
