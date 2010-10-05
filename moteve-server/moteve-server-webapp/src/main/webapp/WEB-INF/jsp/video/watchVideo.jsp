<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ include file="../sys_header.jsp" %>
<html>
    <head>
        <title>Moteve</title>
        <script type="text/javascript" src="../flowplayer/flowplayer-3.1.4.min.js"></script>
    </head>
    <body>
        <%@ include file="../menu.jsp" %>
        <h2>${video.name}</h2>
        author: ${video.author.email}
        <br/>
        date: ${video.creationDate}

        <a href="${streamUrl}"
           style="display:block;width:425px;height:300px;"
           id="player">
        </a>

        <script type="text/javascript">
            flowplayer("player", "../flowplayer/flowplayer-3.1.5.swf", {
                clip: {
                    onLastSecond: function() {
                        //msgs.innerHTML += "Common Clip event listener called<br>";
                        flowplayer("player").addClip("${streamUrl}");
                    }
                }
            });
            flowplayer("player").onError(function(errCode, errMsg) {
                if (errCode == 200) {
                    // Stream not found: i.e. end of our video sequence parts
                    flowplayer("player").stop().close().unload();
                    alert("That's all, folks");
                }
            });
        </script>
    </body>
</html>
