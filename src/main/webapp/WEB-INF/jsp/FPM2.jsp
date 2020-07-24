<!DOCTYPE html>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
<title>Hello WebSocket</title>
<link href="/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="<c:url value="/css/main.css" />" />
<head>
<meta charset="UTF-8">
<title>Hello!</title>
</head>
<body>

	<div id="main-content" class="container">
		<div class="row">
			<div class="col-md-6">
				<form class="form-inline">
					<div class="form-group">
						<a href="/">Back</a>
					</div>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<table id="conversation" class="table table-striped">
					<thead>
						<tr>
							<th>Stats</th>
						</tr>
					</thead>

					<c:forEach items="${max_frequent_item_sets}" var="entry">
						<tr>
							<td style="width: 100%;">${entry.key}</td>
							<td>${entry.value}</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</div>
	</div>

</body>
</html>