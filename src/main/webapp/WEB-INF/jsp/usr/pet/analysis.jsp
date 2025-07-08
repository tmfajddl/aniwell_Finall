<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>분석 결과</title>
</head>
<body>
<h2>펫 분석 결과</h2>
<c:forEach var="item" items="${analysisList}">
  <div style="margin-bottom:20px;">
    <img src="${item.imagePath}" alt="이미지" width="200"><br>
    감정: ${item.emotionResult} <br>
    신뢰도: ${item.confidence * 100}% <br>
    분석 시간: ${item.analyzedAt}
  </div>
</c:forEach>
</body>
</html>
