<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js">
	
</script>
</head>
<body>
	<h2>Everything is generated!</h2>
	<form accept-charset="UTF-8" role="form" method="GET"
		action="<c:url value="/"/>">
		<button id="genHello" type="submit">Generate More Files</button>
	</form>
	<h4></h4>
</body>
</html>
