package computehash;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name = "HashServlet",
        urlPatterns = {"/compute_hashes"})
public class HashServlet extends javax.servlet.http.HttpServlet {
    HashModel hashModel = new HashModel();
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String originalText = request.getParameter("textToHash");
        String hashFunction = request.getParameter("hashFunction");

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

        request.setAttribute("error", false);
        request.setAttribute("errorText", "");

        // If text is blank, display error
        if (originalText.strip().isBlank() || hashFunction.isBlank()) {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "Fill up all the fields and resubmit the form.");

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Get hash values for the text entered and pass it to the view
        Hash hash = hashModel.getHash(originalText, hashFunction);
        request.setAttribute("computedHash", hash);

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
    }


    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        // GET requests are not allowed in this part of the project
        request.setAttribute("error", true);
        request.setAttribute("errorText", "\"GET\" requests and blank calls to this page are not allowed.");
        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
        return;
    }
}
