<%@ page contentType="text/html;charset=UTF-8" %>


<html>
<head>
    <title>Login</title>
    <style>
        body {
            margin: 0;
            background-color: #999;
            font-family: 'Arial', sans-serif;
        }

        .popup-container {
            display: flex;
            width: 600px;
            min-height: 400px;
            margin: 100px auto;
            background: white;
            border-radius: 15px;
            overflow: hidden;
            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
        }

        .left-panel {
            width: 50%;
            padding: 40px 30px;
            background-color: white;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
        }

        .left-panel h1 {
            margin-bottom: 30px;
            font-size: 28px;
        }

        .left-panel input {
            width: 100%;
            margin-bottom: 15px;
            padding: 8px;
            border: none;
            border-bottom: 1px solid #aaa;
            outline: none;
            background: transparent;
        }

        .sign-in-button {
            margin-top: 10px;
            background-color: #f5d76e;
            border: none;
            padding: 10px 20px;
            border-radius: 10px;
            box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
            cursor: pointer;
        }

        .sign-in-button:hover {
            background-color: #f7e08c;
        }

        .right-panel {
            width: 50%;
            background: linear-gradient(135deg, #c0d9c9, #f2eb99);
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
        }

        .sign-up-button {
            background-color: #a8cbb5;
            border: none;
            padding: 10px 20px;
            border-radius: 10px;
            box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
            cursor: pointer;
        }

        .sign-up-button:hover {
            background-color: #b9e0c8;
        }

        .logo-img {
            margin-top: 20px;
            width: 100px;
            opacity: 0.9;
        }
    </style>
</head>
<body>

<div class="popup-container">
    <!-- 로그인 폼 -->
    <form class="left-panel" action="/usr/member/doLogin" method="post">
        <h1>LOGIN</h1>
        <input type="text" name="loginId" placeholder="ID" required>
        <input type="password" name="loginPw" placeholder="PW" required>
        <button class="sign-in-button" type="submit">sign in</button>

        <!-- 카카오 로그인 버튼 (팝업용) -->
        <button type="button" onclick="kakaoLoginPopup()" style="background: none; border: none; margin-top: 15px;">
            <img src="/img/kakao_login_medium_narrow.png" alt="카카오 로그인 버튼"/>
        </button>


        <!-- 로고 이미지 -->
        <img class="logo-img" src="/img/logo.png" alt="Aniwell Logo">
    </form>

    <!-- 회원가입 버튼 -->
    <div class="right-panel">
        <button class="sign-up-button" onclick="location.href='/usr/member/join'">sign up</button>
    </div>
</div>
<script src="https://developers.kakao.com/sdk/js/kakao.js"></script>
<script>
    Kakao.init('${kakaoRestApiKey}');
    console.log('Kakao SDK 초기화됨:', Kakao.isInitialized());

    function kakaoLoginPopup() {
        Kakao.Auth.login({
            scope: 'profile_nickname', // 필요하면 'profile_image,email' 추가
            scope: 'profile_image',
            redirectUri: 'http://localhost:8080/usr/member/login/kakao',
            success: function (authObj) {
                Kakao.API.request({
                    url: '/v2/user/me',
                    success: function (res) {
                        console.log('사용자 정보:', res);

                        fetch('/usr/member/social-login', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json'},
                            body: JSON.stringify({
                                provider: 'kakao',
                                socialId: res.id,
                                name: res.properties?.nickname || '비회원',
                                email: res.kakao_account?.email || ''
                            })
                        }).then(r => r.json())
                            .then(result => {
                                if (result.resultCode === 'S-1') {
                                    location.href = '/';
                                } else {
                                    alert(result.msg || '로그인 실패');
                                }
                            });
                    },
                    fail: function (error) {
                        console.error('사용자 정보 요청 실패', error);
                        alert('사용자 정보 요청 실패');
                    }
                });
            },
            fail: function (err) {
                console.error('카카오 로그인 실패', err);
                alert('카카오 로그인 실패');
            }
        });
    }
</script>


</body>
</html>
