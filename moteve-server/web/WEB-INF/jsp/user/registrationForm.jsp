<%-- 
    Document   : registrationForm
    Created on : Jan 16, 2010, 10:14:55 PM
    Author     : radek
--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Moteve user registration</title>
        <style>
            .error {
                color: #ff0000;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <h1>Moteve user registration</h1>
        <form:form method="POST" commandName="user">
            <form:errors path="*" cssClass="error" />
            <table>
                <tr>
                    <td>email</td>
                    <td><form:input path="email" /></td>
                    <td><form:errors path="email" cssClass="error" /></td>
                </tr>
                <tr>
                    <td>password</td>
                    <td><form:input path="password" /></td>
                    <td><form:errors path="password" cssClass="error" /></td>
                </tr>
                <tr>
                    <td colspan="2"><input type="submit" /></td>
                </tr>
            </table>
        </form:form>
    </body>
</html>
