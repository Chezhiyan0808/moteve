<%--
Anonymous user can choose: Videos (watch, list), Login, Register
Member can choose: Videos (watch, list, manage), Groups (add, remove, manage members), Contacts (add, list), Logout
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="security" %>
<security:authentication property="name" var="loggedUser" />
<div id="main_menu">
    <a href="<c:url value='/video/listVideos.htm'/>">Videos</a>
    <security:authorize ifAnyGranted="ROLE_MEMBER,ROLE_ADMIN">
        | <a href="<c:url value='/group/manageGroups.htm'/>">Groups</a> | <a href="<c:url value='/contact/listContacts.htm'/>">Contacts</a> | ${loggedUser} | <a href="<c:url value='/logout.jsp'/>">Logout</a>
    </security:authorize>
    <security:authorize ifAllGranted="ROLE_ANONYMOUS">
        | <a href="<c:url value='/login.htm'/>">Login</a> | <a href="<c:url value='/register.htm'/>">Register</a>
    </security:authorize>
</div>