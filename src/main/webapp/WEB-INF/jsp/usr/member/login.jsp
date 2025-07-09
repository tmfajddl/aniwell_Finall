<%--
  Created by IntelliJ IDEA.
  User: e-suul
  Date: 25. 7. 9.
  Time: 오전 8:53
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>로그인</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      background-color: #f0f0f0;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      margin: 0;
    }
    .login-container {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
      width: 300px;
    }
    .login-container h2 {
      text-align: center;
      margin-bottom: 20px;
    }
    .login-container input {
      width: 100%;
      padding: 10px;
      margin: 10px 0;
      border-radius: 4px;
      border: 1px solid #ccc;
    }
    .login-container button {
      width: 100%;
      padding: 10px;
      background-color: #4CAF50;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .login-container button:hover {
      background-color: #45a049;
    }
    .error-message {
      color: red;
      font-size: 12px;
      text-align: center;
    }
  </style>
</head>
<body>

<div class="login-container">
  <h2>로그인</h2>

  <!-- 로그인 실패 메시지 표시 -->
  <c:if test="${param.error != null}">
    <div class="error-message">아이디 또는 비밀번호를 확인해주세요.</div>
  </c:if>

  <form action="/usr/member/doLogin" method="post">
    <input type="text" name="loginId" placeholder="아이디" required><br>
    <input type="password" name="loginPw" placeholder="비밀번호" required><br>
    <button type="submit">로그인</button>
  </form>

  <div style="text-align: center; margin-top: 10px;">
    <a href="/usr/member/findLoginId">아이디 찾기</a> |
    <a href="/usr/member/findLoginPw">비밀번호 찾기</a>
  </div>
</div>

</body>
</html>

</body>
</html>
