<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>BLE 활동 기록</title>
    <style>
        body {
            font-family: 'SUIT', sans-serif;
            background: #f7f7f7;
            padding: 20px;
        }

        h2 {
            color: #333;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th, td {
            padding: 12px;
            text-align: center;
            border: 1px solid #ccc;
        }

        th {
            background-color: #ffe6ee;
            color: #333;
        }

        tr:nth-child(even) {
            background-color: #fff8f8;
        }

        tr:hover {
            background-color: #f1f1f1;
        }
    </style>
</head>
<body>

<h2>🐾 BLE 활동 기록 (petId = ${petId})</h2>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Zone</th>
        <th>입장 시각</th>
        <th>퇴장 시각</th>
        <th>머문 시간(초)</th>
        <th>RSSI</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="a" items="${activities}">
        <tr>
            <td>${a.id}</td>
            <td>${a.zoneName}</td>
            <td>${a.enteredAt}</td>
            <td>${a.exitedAt}</td>
            <td>${a.durationSec}</td>
            <td>${a.rssi}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>
