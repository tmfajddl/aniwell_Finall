<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>백신 등록</title>
</head>
<body>
<h1>백신 등록</h1>

<!-- petId 파라미터를 가져오는 방법 -->
<form action="/usr/pet/vaccination/doRegistration?petId=${param.petId}" method="post">

  <label for="vaccineName">백신 이름:</label>
  <select id="vaccineName" name="vaccineName" required>
    <option value="">백신을 선택하세요</option>
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
  <input type="date" id="injectionDate" name="injectionDate" required />

  <button type="submit">등록</button>
</form>
</body>
</html>
