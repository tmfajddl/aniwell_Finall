<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pageTitle" value="MYPAGE" />
<%@ include file="../common/head.jspf" %>

<section class="mt-24 text-lg px-4">
  <div class="mx-auto max-w-2xl bg-white p-6 rounded-xl shadow-md">
    <h1 class="text-2xl font-bold mb-6 text-center">내 정보</h1>
    <table class="w-full table-auto border text-sm">
      <tbody>
        <tr class="border-t">
          <th class="text-left px-4 py-2 w-1/3">아이디</th>
          <td class="px-4 py-2">${rq.loginedMember.loginId}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">이름</th>
          <td class="px-4 py-2">${rq.loginedMember.name}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">닉네임</th>
          <td class="px-4 py-2">${rq.loginedMember.nickname}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">이메일</th>
          <td class="px-4 py-2">${rq.loginedMember.email}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">전화번호</th>
          <td class="px-4 py-2">${rq.loginedMember.cellphone}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">가입일</th>
          <td class="px-4 py-2">${rq.loginedMember.regDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">수정일</th>
          <td class="px-4 py-2">${rq.loginedMember.updateDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">탈퇴여부</th>
          <td class="px-4 py-2">
            <c:choose>
              <c:when test="${rq.loginedMember.delStatus}">탈퇴</c:when>
              <c:otherwise>정상</c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">탈퇴일</th>
          <td class="px-4 py-2">${rq.loginedMember.delDate}</td>
        </tr>
        <tr class="border-t">
          <th class="text-left px-4 py-2">권한 레벨</th>
          <td class="px-4 py-2">
            <c:choose>
              <c:when test="${rq.loginedMember.authLevel == 7}">관리자</c:when>
              <c:when test="${rq.loginedMember.authLevel == 3}">수의사</c:when>
              <c:otherwise>일반회원</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="text-center mt-6">
      <a href="../member/checkPw" class="text-blue-600 underline hover:text-blue-800">회원정보 수정</a>
    </div>
     <div class="text-center mt-4">
      <button class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700" onclick="doWithdraw()">회원 탈퇴</button>
    </div>
    <div class="text-center mt-4">
      <button class="btn" type="button" onclick="history.back()">뒤로가기</button>
    </div>
  </div>
</section>

<script>
function doWithdraw() {
  if (!confirm("정말 탈퇴하시겠습니까? 탈퇴 후에는 복구할 수 없습니다.")) return;

  fetch('/usr/member/doWithdraw', {
    method: 'POST'
  })
  .then(res => res.text())
  .then(scriptText => {
    // 스크립트 내용 추출
    const matched = scriptText.match(/<script>([\s\S]*?)<\/script>/);

    if (matched && matched[1]) {
      const actualScript = matched[1];

      const scriptEl = document.createElement('script');
      scriptEl.textContent = actualScript;
      document.body.appendChild(scriptEl);  // ✅ 이제 진짜로 실행됨!
    } else {
      console.error("스크립트 태그가 응답에 포함되지 않았습니다.");
    }
  });
}

</script>


<%@ include file="../common/foot.jspf"%>
