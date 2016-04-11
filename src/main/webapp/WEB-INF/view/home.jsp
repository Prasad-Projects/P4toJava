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


	<!-- <button id="genHello" type="submit">Generate File</button> -->

	<h4>Attach p4 header specification file</h4>
	<div>
		<input type="file" id="fileInput" />
	</div>

	<h4>Attach p4 graph specification file</h4>
	<div>
		<input type="file" id="graphInput" />
	</div>

	<h4>Attach beautification file (if any)</h4>
	<div>
		<input type="file" id="beauty" />
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
		<h4></h4>
	</div>
	<!-- <button id="genHello">Press to Generate</button> -->

	<form accept-charset="UTF-8" role="form" method="GET"
		action="<c:url value="/index"/>">
		<button id="genHello" type="submit">Generate File</button>
	</form>

	<h4></h4>
	<button id="checker" type="submit">Create Protocol checker</button>
	<h4></h4>
	<button id="clear" type="submit">Start Blank-slate</button>

	<script type="text/javascript">
		var p4header;
		var p4graph;
		var beauty;

		function setBeauty(beautyFile) {
			beauty = beautyFile;
		}

		function setheader(header) {
			p4header = header;
		}

		function setgraph(graph) {
			p4graph = graph;
		}

		function call() {
			var formValues = {
				headerString : p4header,
				graphString : p4graph,
				beautyString : beauty,
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
						data : JSON.stringify(formValues),
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
		$("#clear").click(function() {
			$.ajax({
				url : '/p4tojava/clear',
				type : 'POST',
				contentType : 'application/json; charset=utf-8',
				dataType : 'text',
				data : JSON.stringify(),
				success : function(data) {
					if (data === "success") {
						aler("Cleared everything!")
					} else {
						alert("Error!!!");
					}
				},
				error : function() {
					alert("Something went wrong. Please try again later.");
				}
			});
		});

		$("#genHello").click(function() {

			//Retrieve the first (and only!) File from the FileList object
			var f = document.getElementById("fileInput").files[0];
			var g = document.getElementById("graphInput").files[0];
			var b = document.getElementById("beauty").files[0];

			if (f) {
				var r = new FileReader();
				r.onload = function(e) {
					var p4header = e.target.result;
					setheader(p4header);
				}
				r.readAsText(f);
			} else {
				alert("Failed to load header-spec file");
			}

			if (g) {
				var r = new FileReader();
				r.onload = function(e) {
					var p4graph = e.target.result;
					setgraph(p4graph);
					if (b === undefined) {
						call();
					}
				}
				r.readAsText(g);
			} else {
				alert("Failed to load graph file");
			}

			if (b) {
				var r = new FileReader();
				r.onload = function(e) {
					var beautyFile = e.target.result;
					setBeauty(beautyFile);
					call();
				}
				r.readAsText(b);
			} else {
				setBeauty("NULL");
			}
		});

		$("#checker")
				.click(
						function() {
							var form = {
								path : $("#path").val()
							}
							$
									.ajax({
										url : '/p4tojava/checker',
										type : 'POST',
										contentType : 'application/json; charset=utf-8',
										dataType : 'text',
										data : JSON.stringify(form),
										success : function(data) {
											if (data === "success") {
												alert("Successfully created protocol checker!")
											} else {
												alert("Error!!!");
											}
										},
										error : function() {
											console
													.log("Something went wrong. Please try again later.");
										}
									});
						});
	</script>
</body>
</html>
