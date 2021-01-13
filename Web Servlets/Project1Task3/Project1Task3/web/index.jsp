<%@ page import="java.util.List" %><%--
  Created by IntelliJ IDEA.
  User: Animish
  Date: 2/7/2020
  Time: 12:36 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%= (request.getAttribute("doctype") == null) ? "" : request.getAttribute("doctype") %>
<html>
  <head>
    <title>Distributed Systems Class Clicker | Animish Andraskar | DS Project1Task2</title>
  </head>
  <body>
  <h1>Distributed Systems Class Clicker</h1>
  <% if (request.getAttribute("error") != null && (boolean) request.getAttribute("error")) {
    /*** If any error is sent from the controller ***/
    String errorText = (String) request.getAttribute("errorText");
  %>
  <div style="color: #ff0000; margin-bottom: 25px;">
    <strong>Error:</strong> <%= errorText %>
  </div>
  <% } %>

  <% if (request.getAttribute("resultsPage") != null && (boolean) request.getAttribute("resultsPage")) {
    /*** This is a results page view ***/
    // If no answers are recorded
    if (request.getAttribute("resultsText") == null || request.getAttribute("resultsText").equals("")) { %>
      <p>There are currently no results.</p>
    <% }
    // Else, display the answer frequency and show the appropriate message
    else { %>
      <p>The results from the survey are as follows:</p>
      <%= request.getAttribute("resultsText") %>
      <p>These results now have been cleared.</p>
    <% } %>
    <a href="./"><button>Continue</button></a>

  <% } else {
    /*** This is the default view ***/
  %>
    <%
      // If we have last answer, show it. Otherwise, it is a fresh start
      if (request.getAttribute("lastAnswer") != null) { %>
    <p>Your "<%= request.getAttribute("lastAnswer") %>" response has been registered</p>
    <p>Submit your answer to the next question:</p>
    <% } else { %>
    <p>Submit your answer to the current question:</p>
    <% } %>
    <form method="post" action="/submit">
      <input type="radio" name="answer" value="A"> A<br/>
      <input type="radio" name="answer" value="B"> B<br/>
      <input type="radio" name="answer" value="C"> C<br/>
      <input type="radio" name="answer" value="D"> D<br/><br/>
      <input type="submit">
    </form>
  <% } %>
  </body>
</html>
