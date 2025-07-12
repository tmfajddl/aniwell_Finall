<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
  <title>Aniwell 크루 채팅</title>
  <script src="/webjars/sockjs-client/1.5.1/sockjs.min.js"></script>
  <script src="/webjars/stomp-websocket/2.3.4/stomp.min.js"></script>
  <style>
    body {
      font-family: 'SUIT', sans-serif;
      background: linear-gradient(to bottom, #DBE3A3 30%, #FEE191 60%, #FAD98A 100%);
      margin: 0;
      padding: 20px;
    }
    #chatBox {
      width: 90%;
      max-width: 500px;
      height: 500px;
      margin: auto;
      border-radius: 30px;
      background-color: #ffffffcc;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      padding: 20px;
      overflow-y: scroll;
    }
    .msg {
      display: flex;
      margin: 12px 0;
      align-items: flex-start;
    }
    .msg.me {
      justify-content: flex-end;
    }
    .profile {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #fff0d9;
      margin-right: 12px;
      border: 2px solid #ffe4a3;
    }
    .bubble {
      max-width: 60%;
      padding: 12px;
      background-color: #fff9d4;
      border-radius: 18px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.1);
      position: relative;
      white-space: pre-wrap;
    }
    .msg.me .bubble {
      background-color: #d0f4e1;
    }
    .nickname {
      font-size: 13px;
      font-weight: bold;
      margin-bottom: 4px;
      color: #585858;
    }
    .time {
      font-size: 11px;
      color: #aaa;
      text-align: right;
      margin-top: 4px;
    }
    .date-divider {
      text-align: center;
      color: #888;
      margin: 20px 0 10px;
      font-size: 13px;
    }
    #chatInput {
      width: 80%;
      max-width: 400px;
      padding: 10px;
      border-radius: 20px;
      border: none;
      outline: none;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
    #sendBtn {
      padding: 10px 16px;
      background-color: #A7CFB3;
      color: white;
      border: none;
      border-radius: 20px;
      cursor: pointer;
      margin-left: 8px;
    }
  </style>
</head>
<body>

<h2 style="text-align:center; margin-bottom:20px;">🗨️ 산책 크루 채팅</h2>

<div id="chatBox">
  <!-- 이전 채팅 예시 -->
  <div class="date-divider">2025.07.11</div>

  <div class="msg">
    <img src="/img/logo.png" class="profile">
    <div>
      <div class="nickname">멍멍이엄마</div>
      <div class="bubble">안녕하세요! 산책 시간 맞춰볼까요?</div>
      <div class="time">14:03</div>
    </div>
  </div>

  <div class="msg">
    <img src="/img/logo.png" class="profile">
    <div>
      <div class="nickname">멍멍이엄마</div>
      <div class="bubble">내일 오후 5시 어때요?</div>
      <div class="time">14:04</div>
    </div>
  </div>

  <div class="msg">
    <img src="/img/logo.png" class="profile">
    <div>
      <div class="nickname">멍멍이엄마</div>
      <div class="bubble">장소는 평화공원 앞!</div>
      <div class="time">14:04</div>
    </div>
  </div>
</div>

<br style="clear:both;"/>
<div style="text-align:center; margin-top:20px;">
  <input type="text" id="chatInput" placeholder="메시지를 입력하세요">
  <button id="sendBtn" type="button">전송</button>
</div>

<script>
  let stompClient = null;
  const crewId = 1;
  const senderId = 123;

  const userMap = {
    123: { nickname: "나", profile: "/img/me.jpeg" },
    456: { nickname: "멍멍이엄마", profile: "/img/logo.png" }
  };

  let lastDate = "";

  function connect() {
    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
      stompClient.subscribe("/topic/crew/" + crewId, function (msg) {
        const message = JSON.parse(msg.body);
        renderMessage(message);
      });
    });
  }

  function sendMessage() {
    const input = document.getElementById("chatInput");
    const content = input.value.trim();
    if (content === "") return;

    const message = {
      crewId: crewId,
      senderId: senderId,
      content: content,
      sentAt: new Date().toISOString()
    };

    stompClient.send("/app/chat.send/" + crewId, {}, JSON.stringify(message));
    renderMessage(message);
    input.value = "";
  }

  function handleEnter(e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }

  function renderMessage(msg) {
    const isMe = msg.senderId === senderId;
    const user = userMap[msg.senderId] || { nickname: "알 수 없음", profile: "/img/default.png" };
    const chatBox = document.getElementById("chatBox");

    const msgDate = formatDateOnly(msg.sentAt);
    if (lastDate !== msgDate) {
      const divider = document.createElement("div");
      divider.className = "date-divider";
      divider.textContent = msgDate;
      chatBox.appendChild(divider);
      lastDate = msgDate;
    }

    const msgDiv = document.createElement("div");
    msgDiv.className = "msg" + (isMe ? " me" : "");

    let html = "";
    if (!isMe) {
      html += "<img src='" + user.profile + "' class='profile'>";
    }

    html += "<div>";
    html += "<div class='nickname'>" + user.nickname + "</div>";
    html += "<div class='bubble'>" + msg.content + "</div>";
    html += "<div class='time'>" + formatTime(msg.sentAt) + "</div>";
    html += "</div>";

    msgDiv.innerHTML = html;
    chatBox.appendChild(msgDiv);
    scrollToBottom();
  }

  function formatDateOnly(isoTime) {
    const d = new Date(isoTime);
    return d.getFullYear() + "." + String(d.getMonth() + 1).padStart(2, "0") + "." + String(d.getDate()).padStart(2, "0");
  }

  function formatTime(isoTime) {
    const d = new Date(isoTime);
    return String(d.getHours()).padStart(2, "0") + ":" + String(d.getMinutes()).padStart(2, "0");
  }

  function scrollToBottom() {
    const chatBox = document.getElementById("chatBox");
    setTimeout(function () {
      chatBox.scrollTop = chatBox.scrollHeight;
    }, 30);
  }

  function loadPreviousMessages() {
    fetch("/api/chat/" + crewId + "/messages")
            .then(res => res.json())
            .then(data => {
              data.forEach(renderMessage);
            });
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("sendBtn").addEventListener("click", sendMessage);

    // 🎯 중복 방지용 enter handler 등록
    document.getElementById("chatInput").addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });

    connect();
    loadPreviousMessages();
  });
</script>

</body>
</html>
