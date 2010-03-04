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
        <h2>Videos</h2>
        <div>
            Search criteria
            <form action="listVideos.htm" method="post">
                <table>
                    <tr>
                        <td>Video name:</td><td><input type="text" name="videoName"/></td>
                    </tr>
                    <tr>
                        <td>
                            Author:</td><td><input type="text" name="author" alt="Author's email or display name"/>
                            <security:authorize ifAnyGranted="ROLE_MEMBER,ROLE_ADMIN">
                                Only my videos <input type="checkbox" name="myVideos" />
                            </security:authorize>
                        </td>
                    </tr>
                    <tr>
                        <td>Date from:</td><td><input type="text" name="dateFrom"/> to: <input type="text" name="dateTo"/></td>
                    </tr>
                    <tr>
                        <td>Live</td><td><input type="checkbox" name="live" alt="Videos being streamed just now"/></td>
                    </tr>
                    <tr>
                        <td colspan="2"><input type="submit" value="Find videos"/></td>
                    </tr>
                </table>
            </form>
        </div>
        <br/>
        <div>
            <table>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Author</th>
                    <th>Date</th>
                    <th>Live</th>
                    <th>&nbsp;</th>
                </tr>
                <c:forEach var="video" items="${requestScope.videos}">
                    <tr>
                        <td>${video.id}</td>
                        <td>${video.name}</td>
                        <td>${video.author.email}</td>
                        <td><fmt:formatDate value="${video.creationDate}" pattern="yyyy-MM-dd hh:mm:ss" /></td>
                        <td><input type="checkbox" <c:if test="${video.recordInProgress == true}">checked="true"</c:if> disabled="true" /></td>
                        <td><c:if test="${video.author.email == currentUserEmail}">Edit</c:if>&nbsp;</td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
