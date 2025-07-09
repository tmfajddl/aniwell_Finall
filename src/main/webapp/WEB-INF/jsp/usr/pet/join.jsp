
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="/usr/pet/doJoin" method="post">
  <label for="name">이름:</label>
  <input type="text" id="name" name="name" required />

  <label for="species">종:</label>
  <input type="text" id="species" name="species" required />

  <label for="breed">중성화여부:</label>
  <input type="text" id="breed" name="breed" required />

  <label for="gender">성별:</label>
  <input type="text" id="gender" name="gender" required />

  <label for="birthDate">생일:</label>
  <input type="date" id="birthDate" name="birthDate" required />

  <label for="weight">몸무게:</label>
  <input type="number" step="0.1" id="weight" name="weight" required />

  <button type="submit">등록</button>
</form>

</body>
</html>
