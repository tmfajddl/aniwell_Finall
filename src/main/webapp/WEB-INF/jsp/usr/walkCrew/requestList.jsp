<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<title>크루 참가 신청자 목록</title>
<style>
table {
	width: 80%;
	margin: 20px auto;
	border-collapse: collapse;
}

th, td {
	border: 1px solid #ccc;
	padding: 10px;
	text-align: center;
}

th {
	background-color: #f2f2f2;
}

a.btn {
	background-color: #4CAF50;
	color: white;
	padding: 6px 12px;
	border-radius: 4px;
	text-decoration: none;
}
</style>
</head>
<body>

	<h2 style="text-align: center;">🙋 참가 신청자 목록</h2>

	<table>
		<thead>
			<tr>
				<th>회원 ID</th>
				<th>이름</th>
				<th>신청일</th>
				<th>상세보기</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="applicant" items="${applicants}">
				<tr>
					<td>${applicant.memberid}</td>
					<td>${applicant.membername}</td>
					<td>
						<fmt:formatDate value="${applicant.joinedat}" pattern="yyyy-MM-dd HH:mm" />
					</td>

					<td>
						<a class="btn" href="/usr/walkCrew/requestDetail?crewId=${crewId}&memberId=${applicant.memberid}"> 상세보기 </a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>

	<div style="text-align: center;">
		<a href="/usr/walkCrew/detail/${crewId}">← 크루 상세보기로 돌아가기</a>
	</div>

</body>
</html>
