<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Q&A</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
    <style>
        .faq-box, .my-question-box {
            height: 300px;
            overflow-y: auto;
        }
    </style>
</head>
<body class="bg-gray-100">

<!-- 상단: 자주 묻는 질문 제목 리스트 -->
<div class="faq-box space-y-2 mb-4">
    <c:forEach var="faq" items="${qnas}">
        <div class="bg-gray-100 p-2 rounded hover:bg-gray-200">
            <a href="/usr/qna/list?selectedId=${faq.id}" class="block text-sm font-semibold text-gray-800">
                    ${faq.title}
            </a>
        </div>
    </c:forEach>
</div>

<!-- 하단: 선택된 질문/답변 출력 -->
<div class="bg-white p-4 rounded shadow min-h-[150px]">
    <c:choose>
        <c:when test="${not empty selectedQna}">
            <h3 class="text-md font-bold mb-2">Q. ${selectedQna.title}</h3>
            <p class="text-gray-700 whitespace-pre-line">A. ${selectedQna.body}</p>
        </c:when>
        <c:otherwise>
            <p class="text-gray-400">자세히 볼 질문을 선택해주세요.</p>
        </c:otherwise>
    </c:choose>
</div>

<!-- 오른쪽 질문 등록창 -->
<div class="w-1/4 p-6 bg-white rounded shadow">
    <form id="askForm">
        <div class="mb-4">
            <label class="block font-bold">제목</label>
            <input name="title" type="text" class="w-full border rounded p-2" required>
        </div>
        <div class="mb-4">
            <label class="block font-bold">내용</label>
            <textarea name="body" class="w-full border rounded p-2 h-32" required></textarea>
        </div>
        <div class="mb-4">
            <label>
                <input type="checkbox" name="isSecret">
                비공개
            </label>
        </div>
        <button type="submit" class="w-full bg-blue-500 text-white py-2 rounded">질문 남기기!!!!</button>
    </form>
</div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $('#askForm').on('submit', function (e) {
        e.preventDefault();

        $.post('/usr/qna/doAsk', $(this).serialize(), function (data) {
            if (data.resultCode.startsWith('S-')) {
                alert(data.msg);
                location.reload();
            } else {
                alert(data.msg);
            }
        });
    });
</script>

</body>
</html>
