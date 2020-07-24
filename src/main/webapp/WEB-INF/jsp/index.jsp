<!DOCTYPE html>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
<title>Hello BigData</title>
<link href="/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="<c:url value="/css/main.css" />" />

<script src="/webjars/jquery/jquery.min.js"></script>
<script src="/webjars/sockjs-client/sockjs.min.js"></script>
<script src="/webjars/stomp-websocket/stomp.min.js"></script>
<script src="<c:url value="/js/app.js" />"></script>

</head>
<body>

	<noscript>
		<h2 style="color: #ff0000">Seems your browser doesn't support
			Javascript! Websocket relies on Javascript being enabled. Please
			enable Javascript and reload this page!</h2>
	</noscript>

	<div id="main-content" class="container">
		<div class="row">
			<div class="col-md-6">
				<form class="form-inline">
					<div class="form-group">
						<label for="connect">WebSocket connection:</label>
						<button id="connect" class="btn btn-default" type="submit">Connect</button>
						<button id="disconnect" class="btn btn-default" type="submit"
							disabled="disabled">Disconnect</button>
					</div>
				</form>
			</div>
			<div class="col-md-6">
				<form method="get" class="form-inline"
					action="/filteringdHashtagBatch" id="submitHashtag">
					<div class="form-group">
						<input type="text" id="hashtag" name="hashtag" value=""
							class="form-control" placeholder="Insert the Hashtag here...">
					</div>
					<button id="sentiment" class="btn btn-default" type="submit">GetTweetsAndSentimentAnalysis</button>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6"></div>
			<div class="col-md-6">
				<form method="get" class="form-inline" action="/topMentioned"
					id="submitTopMentioned">
					<button id="topMentioned" class="btn btn-default"
						type="submit">TopMentionedPerson</button>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6"></div>
			<div class="col-md-6">
				<form method="get" class="form-inline" id="submitFPM" action="/FPM">
					<button id="fpm" type="submit" class="btn btn-default">Find
						the hashtags that gives you the best like result!</button>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6"></div>
			<div class="col-md-6">
				<form method="get" class="form-inline" id="submitApriori"
					action="/Apriori">
					<button id="apriori" type="submit" class="btn btn-default">Find
						the hashtags that gives you the best like result (Apriori)</button>
				</form>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<table id="conversation" class="table table-striped">
					<thead>
						<tr>
							<th>Tweets</th>
						</tr>
					</thead>
					<tbody id="greetings">
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>
