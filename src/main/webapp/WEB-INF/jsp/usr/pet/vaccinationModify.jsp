<%--
  Created by IntelliJ IDEA.
  User: e-suul
  Date: 25. 7. 8.
  Time: 오후 9:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="/usr/pet/vaccination/doModify" method="post">
  <input type="hidden" name="vaccinationId" value="${petVaccination.id}" />

  <label for="vaccineName">백신 이름:</label>
  <select id="vaccineName" name="vaccineName" required>
    <option value="${petVaccination.vaccineName}">${petVaccination.vaccineName}</option> <!-- 기존 백신 이름 표시 -->
    <option value="Rabies">Rabies</option>
    <option value="Parvovirus">Parvovirus</option>
    <option value="Distemper">Distemper</option>
    <option value="Feline Distemper">Feline Distemper</option>
    <option value="Feline Leukemia">Feline Leukemia</option>
    <option value="Leptospirosis">Leptospirosis</option>
    <option value="Bordetella">Bordetella</option>
    <option value="Feline Panleukopenia">Feline Panleukopenia</option>
    <option value="FIP">FIP</option>
  </select>

  <label for="injectionDate">접종 날짜:</label>
  <input type="date" id="injectionDate" name="injectionDate" value="${petVaccination.injectionDate}" required />

  <button type="submit">수정</button>
</form>


</body>
</html>
