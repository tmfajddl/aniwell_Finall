<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>반려동물 목록</title>
</head>
<body>
<h1>🐾 내 반려동물 목록</h1>

<table border="1" cellpadding="8">
    <thead>
    <tr>
        <th>이름</th>
        <th>종</th>
        <th>품종</th>
        <th>성별</th>
        <th>생일</th>
        <th>몸무게</th>
        <th>접종 기록</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="pet" items="${pets}">
        <tr>
            <td>${pet.name}</td>
            <td>${pet.species}</td>
            <td>${pet.breed}</td>
            <td>${pet.gender}</td>
            <td>${pet.birthDate}</td>
            <td>${pet.weight} kg</td>
            <td>
                <a href="/usr/pet/vaccination?petId=${pet.id}">📅 접종 보기</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
