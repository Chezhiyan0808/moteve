<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ include file="sys_header.jsp" %>
<html>
    <head>
        <title>Moteve</title>
    </head>
    <body>
        <%@ include file="menu.jsp" %>
        <h2>Welcome to Moteve</h2>
        Today is <fmt:formatDate value="${today}" pattern="yyyy-MM-dd" />.
    </body>
</html>