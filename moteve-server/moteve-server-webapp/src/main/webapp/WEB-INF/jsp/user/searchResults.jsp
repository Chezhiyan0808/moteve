<%-- Displays a table with users' details. Used to desplay search results.
Supports showCheckbox parameter: if its value is true, a checkbox are desplayed on each user entry.
This page is to be included by others. --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<div>
    <table border="1">
        <tr>
            <th>E-mail</th>
            <th>Display Name</th>
            <c:if test="${param.showCheckbox == true}">
                <th>Select</th>
            </c:if>
        </tr>
        <c:forEach var="user" items="${requestScope.users}">
            <tr>
                <td>${user.email}</td>
                <td>${user.displayName}</td>
                <c:if test="${param.showCheckbox == true}">
                    <td><input type="checkbox" name="${param.checkboxName}" value="${user.id}" /></td>
                </c:if>
            </tr>
        </c:forEach>
    </table>
</div>