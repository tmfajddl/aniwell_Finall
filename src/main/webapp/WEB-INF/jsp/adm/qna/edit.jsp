<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>

<section class="p-6">
    <h1 class="text-2xl font-bold mb-4">QnA 질문 수정</h1>

    <form action="/adm/qna/doEdit" method="post" class="space-y-4">
        <input type="hidden" name="id" value="${qna.id}" />

        <div>
            <label class="block mb-1 font-semibold">제목</label>
            <input type="text" name="title" value="${qna.title}" class="w-full border p-2 rounded" required />
        </div>

        <div>
            <label class="block mb-1 font-semibold">내용</label>
            <textarea name="body" rows="8" class="w-full border p-2 rounded" required>${qna.body}</textarea>
        </div>

        <div class="space-x-2">
            <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded">수정 완료</button>
            <a href="/adm/qna/detail?id=${qna.id}" class="text-gray-600 underline">← 돌아가기</a>
        </div>
    </form>
</section>
