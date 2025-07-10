<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>회원가입</title>
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
    .signup-container {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
      width: 400px;
    }
    .signup-container h2 {
      text-align: center;
      margin-bottom: 20px;
    }
    .signup-container input, .signup-container select {
      width: 100%;
      padding: 10px;
      margin: 10px 0;
      border-radius: 4px;
      border: 1px solid #ccc;
    }
    .signup-container button {
      width: 100%;
      padding: 10px;
      background-color: #4CAF50;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .signup-container button:hover {
      background-color: #45a049;
    }
    .error-message {
      color: red;
      font-size: 12px;
      text-align: center;
    }
  </style>
  <script>
    // 전화번호 형식 검사
    function validateForm() {
      const phone = document.querySelector('[name="cellphone"]');
      const phonePattern = /^\d{3}-\d{3,4}-\d{4}$/;

      if (!phonePattern.test(phone.value)) {
        alert("전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000");
        return false;  // 폼 제출 방지
      }

      return true;  // 폼 제출 허용
    }
  </script>
</head>
<body>

<div class="signup-container">
  <h2>회원가입</h2>

  <!-- 에러 메시지 출력 -->
  <c:if test="${param.error != null}">
    <div class="error-message">${param.error}</div>
  </c:if>

  <!-- 회원가입 폼 -->
  <form action="/usr/member/doJoin" method="post" onsubmit="return validateForm()">
    <input type="text" name="loginId" placeholder="아이디" required><br>
    <input type="password" name="loginPw" placeholder="비밀번호" required><br>
    <input type="text" name="name" placeholder="이름" required><br>
    <input type="text" name="nickname" placeholder="닉네임" required><br>
    <input type="text" name="cellphone" placeholder="전화번호" required pattern="\d{3}-\d{3,4}-\d{4}"
           title="전화번호 형식은 000-0000-0000 또는 000-000-0000 형식이어야 합니다."><br>
    <input type="email" name="email" placeholder="이메일" required><br>
    <input type="text" name="address" placeholder="주소" required><br>

    <!-- 권한 이름 선택 -->
    <select id="authName" name="authName" onchange="updateAuthLevel()" required>
      <option value="일반">일반</option>
      <option value="수의사">수의사</option>
    </select><br>

    <!-- 권한 레벨 (자동으로 설정됨) -->
    <input type="hidden" id="authLevel" name="authLevel" value="1"><br> <!-- 기본값: 일반 권한 레벨 -->

    <button type="submit">회원가입</button>
  </form>

  <div style="text-align: center; margin-top: 10px;">
    <a href="/usr/member/login">이미 계정이 있나요? 로그인</a>
  </div>
</div>

</body>
</html>
