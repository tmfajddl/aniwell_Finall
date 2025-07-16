<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>${crew.title}-크루전용카페</title>
<style>
.container {
	width: 80%;
	margin: 30px auto;
}

.header {
	text-align: center;
	margin-bottom: 30px;
}

.menu {
	display: flex;
	justify-content: center;
	gap: 20px;
	margin-bottom: 20px;
}

.menu a {
	text-decoration: none;
	font-weight: bold;
	color: #333;
}

.content-box {
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
	background-color: #f9f9f9;
	margin-bottom: 20px;
}

.section-title {
	font-size: 1.2em;
	font-weight: bold;
	margin-bottom: 10px;
	border-bottom: 1px solid #ccc;
	padding-bottom: 5px;
}

ul.article-preview {
	list-style: none;
	padding: 0;
}

ul.article-preview li {
	margin: 6px 0;
}

.calendar-box {
	margin-top: 30px;
}
</style>
</head>
<body>

<div class="container">
	<div class="header">
		<h1>🏠 [${crew.title}] 전용 크루 공간</h1>
		<p class="subtitle">📌 소개: ${crew.description}</p>
	</div>

	<div class="menu">
		<a href="/usr/article/cafeHome?crewId=${crew.id}">🏠 홈</a>
		<a href="/usr/article/list?crewId=${crew.id}&type=notice">📢 공지사항</a>
		<a href="/usr/article/list?crewId=${crew.id}&type=free">📝 자유게시판</a>
		<a href="/usr/article/list?crewId=${crew.id}&type=gallery">📸 사진첩</a>
		<a href="/usr/article/schedule?crewId=${crew.id}">📅 일정</a>
		<c:if test="${crew != null and crew.leaderId == rq.loginedMemberId}">
			<a href="/usr/walkCrewMember/requestList?crewId=${crew.id}">👥 크루 신청자 리스트</a>
		</c:if>
	</div>

	<!-- ✅ 미리보기 섹션: 공지사항 -->
	<div class="content-box">
		<div class="section-title">📢 최근 공지사항</div>
		<ul class="article-preview">
			<c:forEach var="article" items="${noticeArticles}">
				<li>
					<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}">
						${article.title} (${article.regDate.substring(0,10)})
					</a>
				</li>
			</c:forEach>
			<c:if test="${empty noticeArticles}">
				<li>공지사항이 없습니다.</li>
			</c:if>
		</ul>
	</div>

	<!-- ✅ 자유게시판 미리보기 -->
	<div class="content-box">
		<div class="section-title">📝 최근 자유게시판</div>
		<ul class="article-preview">
			<c:forEach var="article" items="${freeArticles}">
				<li>
					<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}">
						${article.title} (${article.regDate.substring(0,10)})
					</a>
				</li>
			</c:forEach>
			<c:if test="${empty freeArticles}">
				<li>자유 게시글이 없습니다.</li>
			</c:if>
		</ul>
	</div>

	<!-- ✅ 사진첩 미리보기 -->
	<div class="content-box">
		<div class="section-title">📸 최근 사진</div>
		<ul class="article-preview">
			<c:forEach var="article" items="${galleryArticles}">
				<li>
					<a href="/usr/article/detail?id=${article.id}&crewId=${crew.id}">
						${article.title} (${article.regDate.substring(0,10)})
					</a>
				</li>
			</c:forEach>
			<c:if test="${empty galleryArticles}">
				<li>사진이 없습니다.</li>
			</c:if>
		</ul>
	</div>

	<!-- ✅ 일정용 달력 placeholder -->
	<div class="content-box calendar-box">
		<div class="section-title">📅 일정 등록</div>
		<p>달력을 클릭해서 일정을 등록하세요.</p>
		<!-- 나중에 fullCalendar 등 JS 라이브러리 연결 가능 -->
		<div id="calendar" style="height: 300px; border: 1px solid #aaa; background: #fff;"></div>
	</div>
</div>

</body>
</html>
