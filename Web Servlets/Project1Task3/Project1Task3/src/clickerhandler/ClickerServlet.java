package clickerhandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "ClickerServlet",
        urlPatterns = {"/submit", "/getResults"})

public class ClickerServlet extends HttpServlet {
    ClickerModel clickerModel = new ClickerModel();
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getServletPath() == "/getResults") {
            /*** doPost only handles answer recording requests (/submit only). Display an error if someone passes POST requests to /getResults ***/
            request.setAttribute("error", true);
            request.setAttribute("errorText", "\"POST\" requests to this page are not allowed.");
            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Get submitted answer
        String submittedAnswer = request.getParameter("answer");

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

        // If no answer chosen, pass an error
        if (submittedAnswer == null || submittedAnswer.strip().isBlank()) {
            request.setAttribute("error", true);
            String errorText = "Please select an answer and retry.";
            if(clickerModel.lastAnswer != null) {
                errorText = errorText + "<br/><br/>Answer you submitted earlier was: " + clickerModel.lastAnswer;
            }
            request.setAttribute("errorText", errorText);

            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Store the answer
        clickerModel.storeAnswer(submittedAnswer);

        // Send appropriate details to the view
        request.setAttribute("lastAnswer", submittedAnswer);
        request.setAttribute("answersList", clickerModel.answersList);

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*** doGet only handles result display requests (/getResults only). Display an error if someone passes GET requests to /submit ***/
        if (request.getServletPath() == "/submit") {
            request.setAttribute("error", true);
            request.setAttribute("errorText", "\"GET\" requests and blank calls to this page are not allowed.");
            RequestDispatcher dispatcher = request.getRequestDispatcher(
                    "/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Set flag to true so that our view detects controller is responding for what
        request.setAttribute("resultsPage", true);

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

        String resultsText = "";
        // Get all the recorded answers, calculate frequency and generate text using HashSet
        if (clickerModel.answersList.size() != 0) {
            Set<String> distinctAnswers = new HashSet<>(clickerModel.answersList);
            for (String a : distinctAnswers) {
                resultsText = resultsText + a + " : " + Collections.frequency(clickerModel.answersList, a) + "<br/>";
            }
            clickerModel.clearAnswers();
        }

        request.setAttribute("resultsText", resultsText);

        RequestDispatcher dispatcher = request.getRequestDispatcher(
                "/index.jsp");
        dispatcher.forward(request, response);
    }
}
