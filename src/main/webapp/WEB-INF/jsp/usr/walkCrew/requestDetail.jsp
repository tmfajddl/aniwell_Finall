<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<title>참가 신청자 상세 정보</title>
<style>
.container {
	width: 60%;
	margin: 30px auto;
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
	background-color: #f9f9f9;
}

h2 {
	text-align: center;
	margin-bottom: 20px;
}

.field {
	margin: 15px 0;
}

.label {
	font-weight: bold;
	margin-bottom: 5px;
}

.back-link {
	margin-top: 20px;
	display: block;
	text-align: center;
	text-decoration: none;
	color: #333;
}

.back-link:hover {
	text-decoration: underline;
}
</style>
</head>
<body>

	<div class="container">
		<h2>🙋 참가 신청자 상세정보</h2>

		<div class="field">
			<div class="label">회원 ID:</div>
			<div>${applicant.memberid}</div>
		</div>

		<div class="field">
			<div class="label">이름:</div>
			<div>${applicant.membername}</div>
		</div>

		<div class="field">
			<div class="label">닉네임:</div>
			<div>${applicant.membernickname}</div>
		</div>

		<div class="field">
			<div class="label">주소:</div>
			<div>${applicant.memberaddress}</div>
		</div>

		<div class="field">
			<div class="label">권한:</div>
			<div>
				<c:choose>
					<c:when test="${applicant.authlevel == 7}">관리자</c:when>
					<c:when test="${applicant.authlevel == 3}">수의사</c:when>
					<c:otherwise>일반</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="field">
			<div class="label">신청일:</div>
			<div>
				<fmt:formatDate value="${applicant.joinedat}"
					pattern="yyyy-MM-dd HH:mm:ss" />
			</div>
		</div>

		<a class="back-link" href="/usr/walkCrew/requestList?crewId=${crewId}">←
			신청자 목록으로 돌아가기</a>
	</div>

</body>
</html>
