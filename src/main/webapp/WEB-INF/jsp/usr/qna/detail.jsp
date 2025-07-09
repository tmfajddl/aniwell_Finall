<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>질문 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-gray-100 p-8">

<div class="max-w-3xl mx-auto bg-white p-6 rounded shadow">

    <h1 class="text-2xl font-bold mb-2">🙋 ${qna.title}</h1>

    <!-- 등록일 + 비공개 여부 + 답변 여부 -->
    <div class="mb-4 text-sm text-gray-500">
        등록일: ${qna.regDate}
        <c:if test="${qna.secret}">
            <span class="ml-4 text-red-500 font-semibold">🔒 비공개 질문</span>
        </c:if>
        <c:if test="${!qna.secret}">
            <span class="ml-4 text-green-500 font-semibold">🌐 공개 질문</span>
        </c:if>

        <c:choose>
            <c:when test="${qna.answered}">
                <span class="ml-4 text-green-600 font-semibold">✔️ 답변 완료</span>
            </c:when>
            <c:otherwise>
                <span class="ml-4 text-yellow-600 font-semibold">⏳ 답변 대기 중</span>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 질문 내용 -->
    <div class="mb-6 whitespace-pre-line text-gray-800">
        ${qna.body}
    </div>

    <!-- 답변이 있을 경우 -->
    <c:if test="${qna.answered}">
        <div class="bg-green-50 p-4 border-l-4 border-green-400 rounded">
            <strong class="text-green-700">📢 수의사 답변</strong>
            <p class="mt-2 text-gray-800 whitespace-pre-line">
                (답변 내용은 vet_answer 테이블에서 추후 연동)
            </p>
        </div>
    </c:if>

    <c:if test="${rq.loginedMemberId == qna.memberId}">
        <div class="mt-6 space-x-2">
            <a href="/usr/qna/modify?id=${qna.id}" class="text-sm text-white bg-yellow-500 px-3 py-1 rounded hover:bg-yellow-600">수정</a>
            <a href="/usr/qna/doDelete?id=${qna.id}" class="text-sm text-white bg-red-500 px-3 py-1 rounded hover:bg-red-600"
               onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
        </div>
    </c:if>

    <div class="mt-6">
        <a href="/usr/qna/list" class="text-blue-600 hover:underline">← 목록으로 돌아가기</a>
    </div>

</div>

</body>
</html>
