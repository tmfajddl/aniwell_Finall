<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
      align-items: flex-end;
    }

    .msg.me {
      justify-content: flex-end;
    }

    .profile {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      margin-right: 8px;
      object-fit: cover;
      border: 2px solid #eee4c1;
    }

    .profile-placeholder {
      width: 36px;
      height: 36px;
      margin-right: 8px;
    }

    .bubble-wrap {
      max-width: 70%;
    }

    .bubble {
      padding: 10px;
      background-color: #fff9d4;
      border-radius: 18px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.1);
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

<div id="chatBox"></div>

<div style="text-align:center; margin-top:20px;">
  <input type="text" id="chatInput" placeholder="메시지를 입력하세요">
  <button id="sendBtn" type="button">전송</button>
</div>

<c:set var="nickname" value="${loginedMember.nickname}" />
<c:set var="photo" value="${pet.photo}" />
<script>
  const crewId = ${crewId};
  const senderId = ${loginedMember.id};
  const senderNickname = "${fn:escapeXml(nickname)}";
  const senderPhoto = "${fn:escapeXml(photo)}";

  let stompClient = null;
  let lastDate = "";
  let lastGroupKey = "";
  let groupBuffer = [];
  let isSending = false;
  let enterPressed = false;

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
    if (isSending) return;
    isSending = true;

    const input = document.getElementById("chatInput");
    const content = input.value.trim();
    if (content === "") {
      isSending = false;
      return;
    }

    const message = {
      crewId: crewId,
      senderId: senderId,
      nickname: senderNickname,
      content: content,
      photo: senderPhoto
      // sentAt은 절대 여기 넣지 마세요!
    };

    stompClient.send("/app/chat.send/" + crewId, {}, JSON.stringify(message));
    input.value = "";

    setTimeout(() => {
      isSending = false;
    }, 300);
  }


  let messageQueue = [];
  let flushTimeout = null;

  function renderMessage(msg) {
    const msgDate = formatDateOnly(msg.sentAt);
    const timeKey = msg.sentAt.slice(0, 16); // "2025-07-12T17:34"
    const groupKey = msg.senderId + "_" + timeKey;
    const chatBox = document.getElementById("chatBox");

    if (lastDate !== msgDate) {
      const divider = document.createElement("div");
      divider.className = "date-divider";
      divider.textContent = msgDate;
      chatBox.appendChild(divider);
      lastDate = msgDate;
      lastGroupKey = "";
    }

    if (groupKey !== lastGroupKey && groupBuffer.length > 0) {
      renderGroup(groupBuffer);
      groupBuffer = [];
    }

    groupBuffer.push(msg);
    lastGroupKey = groupKey;

    // 메시지를 받을 때마다 렌더링 예약
    if (flushTimeout) clearTimeout(flushTimeout);

    flushTimeout = setTimeout(() => {
      if (groupBuffer.length > 0) {
        renderGroup(groupBuffer);
        groupBuffer = [];
      }
    }, 300); // ← 지연 시간을 충분히 줌
  }



  function renderGroup(messages) {
    if (messages.length === 0) return;

    const firstMsg = messages[0];
    const lastMsg = messages[messages.length - 1];
    const isMe = firstMsg.senderId == senderId;
    const chatBox = document.getElementById("chatBox");

    messages.forEach((msg, index) => {
      const isFirst = index === 0;
      const isLast = index === messages.length - 1;

      const msgDiv = document.createElement("div");
      msgDiv.className = "msg" + (isMe ? " me" : "");

      if (!isMe) {
        if (isFirst) {
          const profileImg = document.createElement("img");
          profileImg.className = "profile";
          profileImg.src = msg.photo || "/img/default-pet.png";
          msgDiv.appendChild(profileImg);
        } else {
          const placeholder = document.createElement("div");
          placeholder.className = "profile-placeholder";
          msgDiv.appendChild(placeholder);
        }
      }

      const bubbleWrap = document.createElement("div");
      bubbleWrap.className = "bubble-wrap";

      if (!isMe && isFirst) {
        const nicknameDiv = document.createElement("div");
        nicknameDiv.className = "nickname";
        nicknameDiv.textContent = msg.nickname || "알 수 없음";
        bubbleWrap.appendChild(nicknameDiv);
      }

      const bubble = document.createElement("div");
      bubble.className = "bubble";
      bubble.textContent = msg.content;
      bubbleWrap.appendChild(bubble);

      if (isLast) {
        const time = document.createElement("div");
        time.className = "time";
        time.textContent = formatTime(msg.sentAt);
        bubbleWrap.appendChild(time);
      }

      msgDiv.appendChild(bubbleWrap);
      chatBox.appendChild(msgDiv);
    });

    // 화면에 그려진 다음에 스크롤이 아래로 내려가게!!!
    requestAnimationFrame(() => {
      scrollToBottom();
    });
  }

  function loadPreviousMessages() {
    fetch("/usr/walkCrew/chat/api/" + crewId + "/messages")
            .then(res => res.json())
            .then(data => {
              data.forEach(renderMessage);
              renderGroup(groupBuffer);
              groupBuffer = [];
              scrollToBottom();
              setTimeout(scrollToBottom, 100);
            });
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
    setTimeout(() => {
      chatBox.scrollTop = chatBox.scrollHeight;
    }, 30);
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("sendBtn").addEventListener("click", sendMessage);

    let isComposing = false;

    document.getElementById("chatInput").addEventListener("compositionstart", () => {
      isComposing = true;
    });

    document.getElementById("chatInput").addEventListener("compositionend", () => {
      isComposing = false;
    });

    document.getElementById("chatInput").addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !e.shiftKey && !isComposing) {
        e.preventDefault();
        if (!enterPressed) {
          enterPressed = true;
          sendMessage();
        }
      }
    });

    document.getElementById("chatInput").addEventListener("keyup", function (e) {
      if (e.key === "Enter") {
        enterPressed = false;
      }
    });

    connect();
    loadPreviousMessages();
  });
</script>

</body>
</html>