<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js">
	
</script>
</head>

<style type="text/css">
#title {
	text-align: center;
	color: blue;
}

#caution {
	color: red;
}
</style>
<body>


	<h2 id="title">Welcome to PacketAnalyzer Javagen Webapp</h2>

	<p>This webapp powers the automation of java code generation from
		relevant P4 files. Just plug in your P4 header specification file and
		voila! It will automatically genreate the java header class marking
		header boundaries in bits and bytes. You can build your PacketAnalyzer
		project with these files (placed appropriately by this app itself!)
		and run your new custom analysis.</p>

	<p id="caution">Remember, the source folder of the project doesn't
		mean "src" folder, it means the folder from where your root package
		starts in the project.</p>


	<form accept-charset="UTF-8" role="form" method="GET"
		action="<c:url value="/index"/>">
		<button id="genHello" type="submit">Generate File</button>
	</form>

	<!-- <button id="genHello" type="submit">Generate File</button> -->

	<div>
		<input type="file" id="fileInput" />
	</div>

	<div>
		<h4>Enter protocol name:</h4>
		<input type="text" id="protocol">
	</div>

	<div>
		<h4>Enter source folder path:</h4>
		<input type="text" id="path">
	</div>

	<div>
		<h4>Enter package name:</h4>
		<input type="text" id="packageName">
	</div>
	<!-- <button id="genHello">Press to Generate</button> -->

	<script type="text/javascript">
		$("#genHello")
				.click(
						function() {

							//Retrieve the first (and only!) File from the FileList object
							var f = document.getElementById("fileInput").files[0];

							if (f) {
								var r = new FileReader();
								r.onload = function(e) {
									var userParseGraph = e.target.result;
									var formValues = {
										headerString : userParseGraph,
										protocol : $("#protocol").val(),
										path : $("#path").val(),
										packageName : $("#packageName").val()
									};
									$
											.ajax({
												url : '/p4tojava/read',
												type : 'POST',
												contentType : 'application/json; charset=utf-8',
												dataType : 'text',
												data : JSON
														.stringify(formValues),
												success : function(data) {
													if (data === "success") {
														console.log("Success!")
													} else {
														alert("Error!!!");
													}
												},
												error : function() {
													console
															.log("Something went wrong. Please try again later.");
												}
											});
								}
								r.readAsText(f);

							} else {
								alert("Failed to load file");
							}

						});
	</script>
</body>
</html>
