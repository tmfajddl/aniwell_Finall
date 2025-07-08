<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>백신 일정 상세보기</title>
</head>
<body>

<h1>📅 백신 일정 상세보기</h1>

<!-- 백신 일정 상세 정보 -->
<table border="1" cellpadding="8">
  <tr>
    <th>백신 이름</th>
    <td>${petVaccination.vaccineName}</td>
  </tr>
  <tr>
    <th>접종 날짜</th>
    <td>${petVaccination.injectionDate}</td>
  </tr>
  <tr>
    <th>다음 접종 예정일</th>
    <td>${petVaccination.nextDueDate}</td>
  </tr>
  <tr>
    <th>수의사 이름</th>
    <td>${petVaccination.vetName}</td>
  </tr>
  <tr>
    <th>비고</th>
    <td>${petVaccination.notes}</td>
  </tr>
</table>

<!-- 수정 및 삭제 버튼 -->
<form action="/usr/pet/vaccination/modify" method="get">
  <input type="hidden" name="vaccinationId" value="${petVaccination.id}" />
  <button type="submit">✏️ 수정</button>
</form>

<form action="/usr/pet/vaccination/delete" method="get" onsubmit="return confirm('정말 삭제하시겠습니까?');">
  <input type="hidden" name="vaccinationId" value="${petVaccination.id}" />
  <button type="submit">❌ 삭제</button>
</form>

</body>
</html>
