<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>질문 상세</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
</head>
<body class="bg-gray-100 p-8">
<div class="max-w-3xl mx-auto bg-white p-6 rounded shadow">
    <h1 class="text-xl font-bold mb-4">${qna.title}</h1>
    <div class="mb-4 text-sm text-gray-500">등록일: ${qna.regDate}</div>

    <div class="mb-6 whitespace-pre-line">
        ${qna.body}
    </div>

    <c:if test="${qna.answered}">
        <div class="bg-gray-100 p-4 border rounded">
            <strong>📢 답변:</strong>
            <p class="mt-2">관리자의 답변이 여기에 들어갑니다. (추후 구현)</p>
        </div>
    </c:if>

    <div class="mt-6">
        <a href="/usr/qna/list" class="text-blue-600 hover:underline">← 목록으로 돌아가기</a>
    </div>
</div>
</body>
</html>
