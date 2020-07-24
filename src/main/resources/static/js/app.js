var stompClient = null;

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
	if (connected) {
		$("#conversation").show();
	}
	else {
		$("#conversation").hide();
	}
	$("#greetings").html("");
}

function connect() {
	var socket = new SockJS('/gs-websocket');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function (frame) {
		setConnected(true);
		console.log('Connected: ' + frame);
		stompClient.subscribe('/topic/tweets', function (tweetMessage) {
			console.log(tweetMessage);
			showGreeting(JSON.parse(tweetMessage.body).content);
		});
	});
}

function disconnect() {
	if (stompClient !== null) {
		stompClient.disconnect();
	}
	setConnected(false);
	console.log("Disconnected");
}

function sendName() {
	stompClient.send("/app/fetch", {});
}

function showGreeting(message) {
	$("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
	$("form").on('submit', function (e) {
		e.preventDefault();
	});
	$( "#connect" ).click(function() { connect(); });
	$( "#disconnect" ).click(function() { disconnect(); });
	$( "#sentiment" ).click(function() { document.getElementById("submitHashtag").submit(); });
	$( "#sentimentactualandold" ).click(function() { document.getElementById("submitCompareTrends").submit(); });
	$( "#fpm" ).click(function() { document.getElementById("submitFPM").submit(); });
	$( "#apriori" ).click(function() { document.getElementById("submitApriori").submit(); });
	$( "#topMentioned" ).click(function() { document.getElementById("submitTopMentioned").submit(); });
});

