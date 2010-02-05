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
        <h2>My Contacts</h2>
        <jsp:include page="../user/searchResults.jsp">
            <jsp:param name="showCheckbox" value="false" />
        </jsp:include>
    </body>
</html>