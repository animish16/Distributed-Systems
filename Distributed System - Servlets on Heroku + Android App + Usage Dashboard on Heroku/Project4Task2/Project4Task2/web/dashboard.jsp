<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %><%--
  Created by IntelliJ IDEA.
  User: animi
  Date: 2020-04-04
  Time: 02:50 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Showtime insights dashboard | Animish Andraskar | Distributed Systems Project 4</title>
    <style>
        table, th, td {
            border: 1px solid black;
        }

        th, td {
            padding-right: 20px;
        }


    </style>
</head>
<body>

<h1>Showtime Analysis Dashboard</h1>
<h2>Statistics</h2>
<h3>Request fulfillment:</h3>
<% if (request.getAttribute("requestStats") != null) {
    String requestStats = (String) request.getAttribute("requestStats");%>
<table>
    <tr>
        <th>Total processed<br/>requests</th>
        <th>Fulfilled<br/>requests</th>
        <th>Unfulfilled<br/>requests</th>
    </tr>
    <%=requestStats%>
</table>
<% } %>

<br/>

<h3>Third party API performance:</h3>
<% if (request.getAttribute("avgReqProcessTime") != null) {
    String avgReqProcessTime = (String) request.getAttribute("avgReqProcessTime"); %>
<table>
    <tr>
        <th>Average request<br/>processing time</th>
    </tr>
    <tr>
        <td>
            <%=avgReqProcessTime%> milliseconds
        </td>
    </tr>
</table>
<% } %>

<br/>

<h3>TV show results:</h3>
<% if (request.getAttribute("topTenShows") != null) {
    String topTenShows = (String) request.getAttribute("topTenShows"); %>
<table>
    <tr>
        <th>Rank</th>
        <th>Show name</th>
        <th>Search Count</th>
    </tr>
    <%=topTenShows%>
</table>
<% } %>

<br/>

<h3>Search keywords:</h3>
<% if (request.getAttribute("topTenSearchStrings") != null) {
    String topTenSearchStrings = (String) request.getAttribute("topTenSearchStrings"); %>
<table>
    <tr>
        <th>Rank</th>
        <th>Search keyword</th>
        <th>Latest matching<br/>result</th>
        <th>Search Count</th>
    </tr>
    <%=topTenSearchStrings%>
</table>
<% } %>

<h2>Complete Request log</h2>
<% if (request.getAttribute("logEntries") != null) {
    String readLogs = (String) request.getAttribute("logEntries"); %>
<table>
    <tr>
        <th rowspan="2">#</th>
        <th colspan="3">Requesting Phone</th>
        <th colspan="2">Result Details</th>
        <th colspan="2">Request Fulfilled</th>
        <th rowspan="2">3rd Party<br/>Response Time</th>
    </tr>
    <tr>
        <th>Manufacturer</th>
        <th>Model</th>
        <th>OS</th>
        <th>Timestamp</th>
        <th>Search Keyword</th>
        <th>Found Show</th>
        <th>Was Request<br/>Successful?</th>
    </tr>
    <%=readLogs%>
</table>
<% } %>
</body>
</html>
