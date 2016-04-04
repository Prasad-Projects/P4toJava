<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
 <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js" >
 </script>
</head>
<body>
<h2>This is the javagen webapp!!</h2>


<form accept-charset="UTF-8" role="form" method="GET" action="<c:url value="/index"/>">
                            <button type="submit">Generate File</button>
                        </form>
<!-- <button id="genHello">Press to Generate</button> -->

<!-- <script type="text/javascript">
    $("#genHello").click(function() {
    	$.get("/p4tojava/index/")
    });

</script> -->
</body>
</html>
