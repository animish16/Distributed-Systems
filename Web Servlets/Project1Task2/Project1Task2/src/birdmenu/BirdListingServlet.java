package birdmenu;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "BirdListingServlet",
        urlPatterns = {"/bird_listing", "/bird_menu"})
public class BirdListingServlet extends HttpServlet {
    BirdListingModel birdListingModel = new BirdListingModel();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {// determine what type of device our user is
        /*** This is bird image display controller ***/
        String chosenBird = request.getParameter("chosenBird");

        /*** Detect user agent and adjust the view accordingly ***/
        String ua = request.getHeader("User-Agent");
        boolean mobile;
        // prepare the appropriate DOCTYPE for the view pages
        if (ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
            mobile = true;
            /*
             * This is the latest XHTML Mobile doctype. To see the difference it
             * makes, comment it out so that a default desktop doctype is used
             * and view on an Android or iPhone.
             */
            request.setAttribute("doctype", "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">");
        } else {
            mobile = false;
            request.setAttribute("doctype", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        }

        /*** If error set before, clear it ***/
        request.setAttribute("error", false);
        request.setAttribute("errorText", "");

        /*** If bird is not chosen (blank post request sent), display an error ***/
        if (chosenBird.strip().isBlank()) {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "No bird is chosen. Please try again.");

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Get bird image link and photographer's name
        String[] birdInfo = birdListingModel.scrapeBird(chosenBird);

        // If the info is null, no bird is there on the website
        if (birdInfo == null) {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "No birds are posted on the page for " + chosenBird.replace("+", " "));

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // If the link field contains String "0", request to zee site failed
        if (birdInfo[0] == "0") {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "Request to the Zoo site failed. Either no internet connection is available or the website is currently down.");

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // If we reached here, everything is working fine. Let's sent the bird image URL and auther's name
        request.setAttribute("birdURL", birdInfo[0]);
        request.setAttribute("birdAuthName", birdInfo[1]);

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*** This is bird image dropdown controller ***/
        // Get the bird name
        String birdName = request.getParameter("birdName");

        /*** Detect user agent and adjust the view accordingly ***/
        String ua = request.getHeader("User-Agent");
        boolean mobile;
        // prepare the appropriate DOCTYPE for the view pages
        if (ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
            mobile = true;
            /*
             * This is the latest XHTML Mobile doctype. To see the difference it
             * makes, comment it out so that a default desktop doctype is used
             * and view on an Android or iPhone.
             */
            request.setAttribute("doctype", "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">");
        } else {
            mobile = false;
            request.setAttribute("doctype", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        }

        // Clear error if already set
        request.setAttribute("error", false);
        request.setAttribute("errorText", "");

        // If bird name field is blank, pass an error
        if (birdName.strip().isBlank()) {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "Fill up all the fields and resubmit the form.");

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Get bird dropdown
        String[] birdMenu = birdListingModel.getBirds(birdName);

        // If nothing found
        if (birdMenu.length == 0) {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "No bird names found.");

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Pass bird menu to the view
        request.setAttribute("birdMenu", birdMenu);

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
    }
}
