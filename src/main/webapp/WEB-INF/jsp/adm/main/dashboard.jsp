<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>관리자 대시보드</title>
    <style>
        body {
            font-family: sans-serif;
            padding: 2rem;
        }

        h1 {
            color: #d9534f;
        }

        ul {
            list-style-type: square;
        }
    </style>
</head>
<body>

<h1>관리자 전용 대시보드</h1>

<p>이 페이지는 관리자만 접근할 수 있습니다.</p>

<ul>
    <li><a href="/adm/qna/list">Q&A 관리</a></li>
    <li><a href="/adm/member/list">회원 목록</a></li>
    <li><a href="/adm/article/list">공지사항 관리</a></li>
</ul>

</body>
</html>
