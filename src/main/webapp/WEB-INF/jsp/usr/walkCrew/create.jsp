<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>크루 등록</title>
<style>
form {
	width: 60%;
	margin: 30px auto;
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
}

label {
	font-weight: bold;
	display: block;
	margin-top: 15px;
}

input[type="text"], textarea, select {
	width: 100%;
	padding: 10px;
	margin-top: 5px;
}

.dong-list button {
	margin: 5px;
	padding: 5px 10px;
}

button[type="submit"] {
	margin-top: 20px;
	padding: 10px 20px;
	background-color: #4CAF50;
	color: white;
	border: none;
	border-radius: 5px;
}
</style>
</head>
<body>

	<h2 style="text-align: center;">🚀 새 크루 등록</h2>

	<form action="/usr/walkCrew/doCreate" method="post">

		<label for="title">제목</label>
		<input type="text" name="title" id="title" required />

		<label for="descriptoin">설명</label>
		<textarea name="descriptoin" id="descriptoin" rows="5" required></textarea>

		<!-- 지역 구 선택 -->
		<label for="area">지역(구)</label>
		<select id="area" name="area" required>
			<option value="">-- 구 선택 --</option>
			<option value="서구">서구</option>
			<option value="중구">중구</option>
			<option value="유성구">유성구</option>
			<!-- 동적으로 받아오려면 JS에서 fetch 로 삽입 -->
		</select>

		<!-- 동 리스트 출력 (선택된 구에 따라) -->
		<label>동 선택</label>
		<div class="dong-list" id="dongList">
			<small>지역(구)를 선택하면 해당 동네가 표시됩니다</small>
		</div>
		<input type="hidden" name="dong" id="selectedDong" />

		<!-- 작성자 ID는 로그인 세션에서 가져오고 hidden 처리 -->
		<input type="hidden" name="leaderId" value="${rq.loginedMemberId}" />

		<button type="submit">등록</button>
	</form>

	<div style="text-align: center;">
		<a href="/usr/walkCrew/list">← 목록으로 돌아가기</a>
	</div>

	<script>
    document.querySelector("#area").addEventListener("change", function () {
        const district = this.value;
        const dongListDiv = document.querySelector("#dongList");
        dongListDiv.innerHTML = "불러오는 중...";

        fetch(`/usr/api/dongList?district=${district}`)
            .then(res => res.json())
            .then(data => {
                dongListDiv.innerHTML = "";

                if (data.length === 0) {
                    dongListDiv.innerHTML = "해당 지역의 동 정보가 없습니다.";
                    return;
                }

                data.forEach(dong => {
                    const btn = document.createElement("button");
                    btn.type = "button";
                    btn.textContent = dong;
                    btn.onclick = () => {
                        document.querySelector("#selectedDong").value = dong;

                        // 선택 시 강조
                        document.querySelectorAll("#dongList button").forEach(b => b.style.backgroundColor = "");
                        btn.style.backgroundColor = "#4CAF50";
                        btn.style.color = "white";
                    };
                    dongListDiv.appendChild(btn);
                });
            });
    });
</script>

</body>
</html>
