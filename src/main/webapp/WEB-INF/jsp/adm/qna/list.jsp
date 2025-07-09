<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>

<section class="p-6">
  <h1 class="text-2xl font-bold mb-4">QnA 목록 (관리자)</h1>
  <table class="table-auto w-full border">
    <thead class="bg-gray-100">
      <tr>
        <th class="border px-4 py-2">ID</th>
        <th class="border px-4 py-2">제목</th>
        <th class="border px-4 py-2">작성자</th>
        <th class="border px-4 py-2">작성일</th>
        <th class="border px-4 py-2">답변</th>
        <th class="border px-4 py-2">관리</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="qna" items="${qnaList}">
        <tr>
          <td class="border px-4 py-2">${qna.id}</td>
          <td class="border px-4 py-2">${qna.title}</td>
          <td class="border px-4 py-2">${qna.memberName}</td>
          <td class="border px-4 py-2">${qna.regDate}</td>
          <td class="border px-4 py-2">
            <c:choose>
              <c:when test="${qna.hasAnswer}">
                <span class="text-green-600">완료</span>
              </c:when>
              <c:otherwise>
                <span class="text-red-600">미답변</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td class="border px-4 py-2 space-x-2">
            <a href="/adm/qna/detail?id=${qna.id}" class="text-blue-600 underline">상세</a>
            <a href="/adm/qna/edit?id=${qna.id}" class="text-yellow-600 underline">수정</a>
            <form action="/adm/qna/doDelete" method="post" style="display:inline;">
              <input type="hidden" name="id" value="${qna.id}" />
              <button type="submit" class="text-red-600 underline bg-transparent border-none" onclick="return confirm('정말 삭제할까요?')">삭제</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</section>
