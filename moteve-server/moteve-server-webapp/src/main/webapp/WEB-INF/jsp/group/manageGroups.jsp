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
        <h2>Groups</h2>
        <form action="removeGroups.htm" method="post">
            <div>
                Existing groups:
                <table border="1">
                    <tr>
                        <th>Name</th>
                        <th>Select</th>
                    </tr>
                    <c:forEach var="group" items="${requestScope.groups}">
                        <tr>
                            <td><a href="<c:url value='manageGroupMembers.htm?groupId=${group.id}'/>">${group.name}</a></td>
                            <td><input type="checkbox" name="selectedGroups" value="${group.id}" /></td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <input type="submit" value="Remove Selected Groups" />
        </form>

        <div>
            <form action="createGroup.htm" method="post">
                Group name: <input type="text" name="groupName" />
                <input type="submit" value="Create Group" />
            </form>
        </div>

    </body>
</html>