<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>
    <title>게시판 목록</title>
</head>
<body>

<h1>${board.name} 게시판</h1>

<table border="1">
    <thead>
    <tr>
        <th>번호</th>
        <th>제목</th>
        <th>작성자</th>
        <th>등록일</th>
        <th>조회수</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="article" items="${articles}">
        <tr>
            <td>${article.id}</td>
            <td>
                <a href="/usr/article/detail?boardId=${boardId}&id=${article.id}">
                        ${article.title}
                </a>
            </td>
            <td>${article.extra__writer}</td>
            <td>${article.regDate}</td>
            <td>${article.hitCount}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<!-- 로그인한 사용자만 글쓰기 버튼 표시 -->
<c:if test="${rq.logined}">
    <a href="/usr/article/write?boardId=${boardId}">글쓰기</a>
</c:if>

</body>
</html>
