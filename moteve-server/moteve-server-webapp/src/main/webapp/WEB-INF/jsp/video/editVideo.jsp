<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ include file="../sys_header.jsp" %>
<html>
    <head>
        <title>Moteve</title>
    </head>
    <body>
        <script type="text/javascript">
            function confirmRemove() {
                if (confirm("Are you sure you want to remove the video?")) {
                    document.removeVideoForm.submit();
                }
            }
            
            function submitPermissions(groups, contacts) {
                return submitAllItems(groups) && submitAllItems(contacts);
            }

            var NS4 = (navigator.appName == "Netscape" && parseInt(navigator.appVersion) < 5);

            function addOption(theSel, theText, theValue)
            {
                var newOpt = new Option(theText, theValue);
                var selLength = theSel.length;
                theSel.options[selLength] = newOpt;
            }

            function deleteOption(theSel, theIndex)
            {
                var selLength = theSel.length;
                if(selLength > 0)
                {
                    theSel.options[theIndex] = null;
                }
            }

            function moveOptions(theSelFrom, theSelTo)
            {

                var selLength = theSelFrom.length;
                var selectedText = new Array();
                var selectedValues = new Array();
                var selectedCount = 0;

                var i;

                // Find the selected Options in reverse order
                // and delete them from the 'from' Select.
                for(i = selLength - 1; i >= 0; i--)
                {
                    if(theSelFrom.options[i].selected)
                    {
                        selectedText[selectedCount] = theSelFrom.options[i].text;
                        selectedValues[selectedCount] = theSelFrom.options[i].value;
                        deleteOption(theSelFrom, i);
                        selectedCount++;
                    }
                }

                // Add the selected text/values in reverse order.
                // This will add the Options to the 'to' Select
                // in the same order as they were in the 'from' Select.
                for(i = selectedCount - 1; i >= 0; i--)
                {
                    addOption(theSelTo, selectedText[i], selectedValues[i]);
                }

                if(NS4) history.go(0);
            }

            function submitAllItems(sel)
            {
                // mark all select's options as selected so they will be submitted to the server
                for(i = sel.length - 1; i >= 0; i--)
                {
                    sel.options[i].selected = true;
                }
                return true;
            }
        </script>
        <%@ include file="../menu.jsp" %>
        <h2>Edit video</h2>
        <div>
            <form name="settingsForm" action="updateVideoSettings.htm?id=${video.id}" method="post" onsubmit="return submitPermissions(videoGroups, videoContacts)">
                Video name: <input type="text" name="videoName" value="${video.name}"/>
                <h3>Video permissions</h3>
                <h4>Groups</h4>
                <table border="1">
                    <tr>
                        <th>Available groups</th>
                        <th>&nbsp;</th>
                        <th>Allowed groups</th>
                    </tr>
                    <tr>
                        <td>
                            <select id="availableGroups" name="availableGroups" multiple="true">
                                <c:forEach items="${requestScope.availableGroups}" var="availableGroup">
                                    <option value="${availableGroup.id}">${availableGroup.name}</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td align="center" valign="middle">
                            <input type="button" value="--&gt;" onclick="moveOptions(this.form.availableGroups, this.form.videoGroups);" />
                            <br />
                            <input type="button" value="&lt;--" onclick="moveOptions(this.form.videoGroups, this.form.availableGroups);" />
                        </td>
                        <td>
                            <select id="videoGroups" name="videoGroups" multiple="true">
                                <c:forEach items="${requestScope.videoGroups}" var="videoGroup">
                                    <option value="${videoGroup.id}">${videoGroup.name}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                </table>

                <h4>Users</h4>
                <table border="1">
                    <tr>
                        <th>Available contacts</th>
                        <th>&nbsp;</th>
                        <th>Allowed contacts</th>
                    </tr>
                    <tr>
                        <td>
                            <select id="availableContacts" name="availableContacts" multiple="true">
                                <c:forEach items="${requestScope.availableContacts}" var="availableContact">
                                    <option value="${availableContact.id}">${availableContact.email}</option>
                                </c:forEach>
                            </select>
                        </td>
                        <td align="center" valign="middle">
                            <input type="button" value="--&gt;" onclick="moveOptions(this.form.availableContacts, this.form.videoContacts);" />
                            <br />
                            <input type="button" value="&lt;--" onclick="moveOptions(this.form.videoContacts, this.form.availableContacts);" />
                        </td>
                        <td>
                            <select id="videoContacts" name="videoContacts" multiple="true">
                                <c:forEach items="${requestScope.videoContacts}" var="videoContact">
                                    <option value="${videoContact.id}">${videoContact.email}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                </table>

                <br />
                <input type="submit" value="Update video settings" />
            </form>
        </div>
        <br/>
        <div>
            <form name="removeVideoForm" action="removeVideo.htm?id=${video.id}" method="post">
                <input type="button" name="removeVideo" value="Remove video" onclick="confirmRemove()" />
            </form>
        </div>
    </body>
</html>
