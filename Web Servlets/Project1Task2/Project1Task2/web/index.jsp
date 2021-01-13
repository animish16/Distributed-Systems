<%--
  Created by IntelliJ IDEA.
  User: Animish
  Date: 1/30/2020
  Time: 12:37 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%= (request.getAttribute("doctype") == null) ? "" : request.getAttribute("doctype") %>
<html>
<head>
  <title>Migratory Bird Search | Animish Andraskar | DS Project1Task2</title>
</head>
<body>
<h1>Migratory Birds</h1>
<div style="margin-bottom: 25px;"> by Animish Andraskar</div>

<% if (request.getAttribute("birdURL") != null) {
  /*** If birdURL is present, this is bird image view ***/
  String birdURL = (String) request.getAttribute("birdURL");
  String birdAuthName = (String) request.getAttribute("birdAuthName");
%>
<h2>Chosen bird: <%= (String) request.getParameter("chosenBird").replace("+", " ") %></h2>
<div>
  <img src="<%= birdURL %>"><br/>
  <p><%= birdURL %></p>
  <p>Photographer: <%=birdAuthName%></p><br/><br/>
  <a href="./"><button>Continue</button></a>
</div>

<% } else if (request.getAttribute("birdMenu") != null) {
  /*** This is bird dropdown view ***/
  String[] birdMenu = (String[]) request.getAttribute("birdMenu");
%>
<div>
  <form method="post" action="/bird_listing">
    <table>
      <tr>
        <td>
          <strong>Choose a bird from the dropdown</strong>
        </td>
        <td>
          <select name="chosenBird">
            <%
              for (String bird : birdMenu) {
            %>
            <option value="<%=bird.replaceAll(" ", "+")%>"><%=bird%></option>
            <%
              }
            %>
          </select>
        </td>
      </tr>
      <tr>
        <td colspan="2">
          <div style="text-align: center;">
            <input type="submit" value="Submit"><br/><br/>
            <a href="./">or start over</a>
          </div>
        </td>
      </tr>
    </table>
  </form>
</div>

<% } else {
  /*** This is our default view ***/
  if (request.getAttribute("error") != null && (boolean) request.getAttribute("error")) {
    /*** If any error is sent from the controller ***/
  String errorText = (String) request.getAttribute("errorText");
%>
<div style="color: #ff0000; margin-bottom: 25px;">
  <strong>Error:</strong> <%= errorText %>
</div>
<% } %>
<form method="get" action="/bird_menu">
  <table>
    <tr>
      <td style="width:210px;">
        <strong>
          Enter the name of a bird:
        </strong>
      </td>
      <td style="width:150px;">
        <input type="text" name="birdName">
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <div style="text-align: center;">
          <input type="submit" value="Submit">
        </div>
      </td>
    </tr>
  </table>
</form>
<% } %>
</body>
</html>