var stompClient = null;
var auxFrame = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#notifications").html("");
}

function connect() {
    var socket = new WebSocket("ws://localhost:9020/communication/reportnet-websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({token: $("#token")[0].value}, function (frame) {
        setConnected(true);
        $("#userName").text(frame.headers["user-name"]);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/notifications', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    $("#userName").text("null");
    console.log("Disconnected");
}

function showGreeting(message) {
    $("#notifications").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
});