<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>게시글 상세보기</title>
</head>
<body>

<h1>[${board.name}] ${article.title}</h1>

<hr>

<p><strong>글 번호:</strong> ${article.id}</p>
<p><strong>작성자:</strong> ${article.extra__writer}</p>
<p><strong>작성일:</strong> ${article.regDate}</p>
<p><strong>수정일:</strong> ${article.updateDate}</p>
<p><strong>조회수:</strong> ${article.hitCount}</p>
<p><strong>추천:</strong> ${article.goodReactionPoint}</p>
<p><strong>비추천:</strong> ${article.badReactionPoint}</p>

<hr>

<h3>내용</h3>
<div>
    ${article.body}
</div>

<hr>

<!-- 디버깅용 로그인 정보 -->
<p><strong>로그인 회원 ID:</strong> ${rq.loginedMemberId}</p>

<!-- 수정/삭제 버튼 (권한 있을 경우만 출력) -->
<c:if test="${article.userCanModify}">
    <a href="/usr/article/modify?id=${article.id}&boardId=${article.boardId}">[수정]</a>
    <a href="/usr/article/doDelete?id=${article.id}&boardId=${article.boardId}">[삭제]</a>
</c:if>

<!-- 목록으로 돌아가기 -->
<a href="/usr/article/list?boardId=${board.id}">[목록]</a>

</body>
</html>
