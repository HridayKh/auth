<%--
  Created by IntelliJ IDEA.
  User: hridaykh
  Date: 11/11/25
  Time: 2:02 pm
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.servlet.http.Cookie" %>
<html>
<head>
    <title>Request Info</title>
</head>
<body>
<h2>Request Information</h2>
<p><strong>Request URL:</strong> <%= request.getRequestURL().toString() %></p>
<p><strong>HTTP Method:</strong> <%= request.getMethod() %></p>

<h3>Headers</h3>
<ul>
<%
    java.util.Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        String headerValue = request.getHeader(headerName);
%>
    <li><strong><%= headerName %>:</strong> <%= headerValue %></li>
<%
    }
%>
</ul>

<h3>Body</h3>
<pre>
<%
    if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
        StringBuilder body = new StringBuilder();
        String line;
        java.io.BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            body.append(line).append("\n");
        }
%><%= body.toString() %><%
    } else {
%><%= "(No body for this request method)" %><%
    }
%>
</pre>

<h3>Session Attributes</h3>
<ul>
<%
    if (session != null) {
        java.util.Enumeration<String> attrNames = session.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            Object attrValue = session.getAttribute(attrName);
%>
    <li><strong><%= attrName %>:</strong> <%= attrValue %></li>
<%
        }
    } else {
%>
    <li>(No session)</li>
<%
    }
%>
</ul>

<h3>Client-side Cookies</h3>
<ul>
<%
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
%>
    <li><strong><%= cookie.getName() %>:</strong> <%= cookie.getValue() %></li>
<%
        }
    } else {
%>
    <li>(No cookies)</li>
<%
    }
%>
</ul>

<h3>Server-side Cookies (Set-Cookie Headers)</h3>
<ul>
    <li>(Server-side cookies are set via response.addCookie() in servlets; not directly readable here)</li>
</ul>
</body>
</html>
