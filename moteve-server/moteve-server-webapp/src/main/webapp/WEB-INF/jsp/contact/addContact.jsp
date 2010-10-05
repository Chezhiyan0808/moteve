<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ include file="../sys_header.jsp" %>
<html>
    <head>
        <title>Moteve</title>
    </head>
    <body>
        <%@ include file="../menu.jsp" %>
        <%@ include file="menu.jsp" %>
        <h2>Add Contact</h2>



        <form action="searchUsers.htm" method="post">
            Search criteria: <input type="text" name="searchCriteria" />
            <input type="submit" value="Find users" />
        </form>

        <form action="addContact.htm" method="post">
            Found users matching the criteria:
            <jsp:include page="../user/searchResults.jsp">
                <jsp:param name="showCheckbox" value="true" />
                <jsp:param name="checkboxName" value="selectedUsers" />
            </jsp:include>
            <input type="submit" value="Add to Contacts" />
        </form>

    </body>
</html>