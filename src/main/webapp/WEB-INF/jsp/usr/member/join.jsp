<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>회원가입</title>
    <style>
        body {
            background-color: #999;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            font-family: 'Arial';
        }

        .signup-wrapper {
            width: 700px;
            max-height: 90vh; /* ✅ 높이 제한 */
            background: white;
            border-radius: 15px;
            display: flex;
            overflow: hidden;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
        }

        .left-panel {
            width: 40%;
            background: linear-gradient(135deg, #f0eb94, #bdd1c6);
            display: flex;
            align-items: flex-end;
            justify-content: center;
            padding: 20px;
        }

        .left-panel button {
            background-color: #f5d76e;
            border: none;
            padding: 10px 20px;
            border-radius: 10px;
            cursor: pointer;
            font-weight: bold;
        }

        .form-panel {
            width: 60%;
            padding: 30px;
            position: relative;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            overflow-y: auto; /* ✅ 스크롤 생기도록 */
        }

        .form-step {
            display: none;
            flex-direction: column;
            gap: 12px;
            animation: fadeIn 0.3s ease-in;
        }

        .form-step.active {
            display: flex;
        }

        input, select {
            border: none;
            border-bottom: 1px solid #aaa;
            background: transparent;
            padding: 8px;
            outline: none;
        }

        button.next-button, button.prev-button, button.submit-button {
            background-color: #a8cbb5;
            border: none;
            padding: 10px;
            border-radius: 10px;
            margin-top: 10px;
            cursor: pointer;
            font-weight: bold;
        }

        .logo-img {
            width: 80px;
            margin-top: 20px;
            display: block;
            margin-left: auto;
            margin-right: auto;
        }

        .timeline {
            margin-top: 20px;
            display: flex;
            justify-content: center;
            gap: 10px;
        }

        .paw-icon {
            width: 24px;
        }

        @
        keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(10px);
            }

            to {
                opacity: 1;
                transform: translateY(0);
            }

        }

        .error-message {
            color: red;
            text-align: center;
            font-size: 13px;
            margin-bottom: 10px;
        }
    </style>


    <!-- 다음 주소 API -->
    <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    <script>
        function sample4_execDaumPostcode() {
            new daum.Postcode(
                {
                    oncomplete: function (data) {
                        let roadAddr = data.roadAddress;
                        let extraRoadAddr = '';
                        if (data.bname
                            && /[\uB3D9|\uB85C|\uAC00]$/g.test(data.bname))
                            extraRoadAddr += data.bname;
                        if (data.buildingName && data.apartment === 'Y')
                            extraRoadAddr += (extraRoadAddr ? ', '
                                + data.buildingName : data.buildingName);
                        if (extraRoadAddr)
                            extraRoadAddr = ' (' + extraRoadAddr + ')';

                        document.getElementById('sample4_postcode').value = data.zonecode;
                        document.getElementById('sample4_roadAddress').value = roadAddr;
                        document.getElementById('sample4_jibunAddress').value = data.jibunAddress;


                        // ✅ address hidden 필드에 도로명주소 + 추가 주소 넣기
                        setTimeout(() => {
                            const fullAddress = roadAddr + (extraRoadAddr ? ' ' + extraRoadAddr : '');
                            const addressInput = document.getElementById('address');

                            if (addressInput) {
                                addressInput.value = fullAddress;
                            } else {
                                console.warn("❗ 'address' input이 존재하지 않습니다.");
                            }
                        }, 150); // 지연으로 input 렌더링 보장


                        // 가이드박스 처리 (필요하면 유지)
                        const guideTextBox = document.getElementById("guide");
                        if (data.autoRoadAddress) {
                            const expRoadAddr = data.autoRoadAddress
                                + extraRoadAddr;
                            guideTextBox.innerHTML = '(예상 도로명 주소 : '
                                + expRoadAddr + ')';
                            guideTextBox.style.display = 'block';
                        } else if (data.autoJibunAddress) {
                            const expJibunAddr = data.autoJibunAddress;
                            guideTextBox.innerHTML = '(예상 지번 주소 : '
                                + expJibunAddr + ')';
                            guideTextBox.style.display = 'block';
                        } else {
                            guideTextBox.innerHTML = '';
                            guideTextBox.style.display = 'none';
                        }
                    }
                }).open();
        }
    </script>

    <script>
        function goToStep(step) {
            document.getElementById('step1').classList.remove('active');
            document.getElementById('step2').classList.remove('active');
            document.getElementById('step' + step).classList.add('active');

            // 발바닥 이미지 교체
            document.getElementById('paw1').src = step === 1 ? '/img/paw_active.png'
                : '/img/paw_inactive.png';
            document.getElementById('paw2').src = step === 2 ? '/img/paw_active.png'
                : '/img/paw_inactive.png';
        }


        function validateForm() {
            const phone = document.querySelector('[name="cellphone"]');
            const phonePattern = /^\d{3}-\d{3,4}-\d{4}$/;
            if (!phonePattern.test(phone.value)) {
                alert("전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000");
                return false;
            }

            const addressInput = document.getElementById("address");
            console.log("🚨 address.value:", addressInput?.value);

            if (!addressInput || addressInput.value.trim() === "") {
                alert("주소를 입력해주세요. 우편번호 찾기를 사용하세요.");
                return false;
            }
            return true;
        }

        // step1에서 Enter 키로 다음 단계 이동
        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('#step1 input').forEach(input => {
                input.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        goToStep(2);
                    }
                });
            });
        });

    </script>
</head>
<body>

<div class="signup-wrapper">
    <!-- 왼쪽 패널 -->
    <div class="left-panel">
        <button onclick="location.href='/usr/member/login'">sign up</button>
    </div>

    <!-- 오른쪽 패널 -->
    <form class="form-panel" action="/usr/member/doJoin" method="post" onsubmit="return validateForm()">
        <c:if test="${param.error != null}">
            <div class="error-message">${param.error}</div>
        </c:if>

        <!-- STEP 1 -->
        <div class="form-step active" id="step1">
            <h2>Create Account</h2>
            <input type="text" name="loginId" placeholder="ID" required>
            <input type="password" name="loginPw" placeholder="PW" required>
            <input type="text" name="name" placeholder="NAME" required>
            <input type="text" name="nickname" placeholder="NICKNAME" required>
            <button type="button" class="next-button" onclick="goToStep(2)">다음</button>
        </div>


        <!-- STEP 2 -->
        <div class="form-step" id="step2">
            <h2>Contact Info</h2>
            <input type="text" name="cellphone" placeholder="PHONE NUMBER (000-0000-0000)" required>
            <input type="email" name="email" placeholder="EMAIL" required>
            <!-- 주소 -->
            <div>
                <input type="hidden" name="address" id="address">
                <label class="block text-sm font-medium mb-1">주소</label>
                <div class="space-y-2">
                    <!-- 우편번호 + 버튼 -->
                    <div class="flex gap-2">
                        <input class="input input-bordered w-40" type="text" name="postcode" id="sample4_postcode"
                               placeholder="우편번호"
                               readonly/>
                        <button type="button" class="btn btn-outline btn-sm" onclick="sample4_execDaumPostcode()">우편번호
                            찾기
                        </button>
                    </div>

                    <!-- 도로명주소 -->
                    <input class="input input-bordered w-full" type="text" name="roadAddress" id="sample4_roadAddress"
                           placeholder="도로명주소" readonly/>

                    <!-- 지번주소 -->
                    <input class="input input-bordered w-full" type="text" name="jibunAddress" id="sample4_jibunAddress"
                           placeholder="지번주소" readonly/>
                </div>
            </div>
            <select name="authName" required>
                <option value="일반">일반</option>
                <option value="수의사">수의사</option>
            </select>

            <button type="button" class="prev-button" onclick="goToStep(1)">뒤로가기</button>
            <button type="submit" class="submit-button">sign up</button>
        </div>

        <!-- 발바닥 타임라인 -->
        <div class="timeline">
            <img id="paw1" class="paw-icon" src="/img/paw_active.png">
            <img id="paw2" class="paw-icon" src="/img/paw_inactive.png">
        </div>

        <!-- 로고 -->
        <img class="logo-img" src="/img/logo.png" alt="Aniwell Logo">
    </form>
</div>

</body>
</html>
