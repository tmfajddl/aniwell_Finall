window.addEventListener("load",start)

function start(){
    
/* ===========================
    Elements Selectors
============================ */

  const elm = {
    arrow: document.querySelector(".form-container__arrow"),
    overlay: document.querySelector(".overlay"),
    title: document.querySelector(".title"),
    signUpButton: document.querySelector(".buttons__signup"),
    loginButton: document.querySelector(".buttons__signup--login"),
    loginForm: document.querySelector(".login-form"),
    registerForm: document.querySelector(".login-form--register")
  }
  
/* ===========================
    Properties Object
============================ */

  const props = {
    left: "left: 20px;",
    bottom: "bottom: -500px;",
    transition1: "transition: bottom 1s;",
    transition2: "transition: bottom 2s;",
    opacity0: "opacity: 0;",
    opacity1: "opacity: 1;",
    trnsDelay: "transition-delay: 1s;",
    zIndex: "z-index: 6;",
    left0: "left: 0;",
    trnsDelay0: "transition-delay: 0s;",
    zIndex0: "z-index: 0;",
    leftM120: "left: -120px;"
  }
  
/* ===========================
    Elements Array
============================ */

  const elms = [elm.arrow, elm.overlay, elm.title, elm.signUpButton, elm.loginButton, elm.loginForm, elm.registerForm]
  
  function transition (elements, props){
    for (let i = 0; i < elements.length; i++){
      elements[i].setAttribute("style", `${props[i]}`)
    }
  } 

/* ===========================
    Events
============================ */

  document.getElementById("signUp").onclick = function (){

    const properties = [props.left, props.opacity0, props.opacity0, `${props.transition1} ${props.bottom}`, `${props.transition2} ${props.bottom}`, props.opacity0, `${props.opacity1} ${props.trnsDelay} ${props.zIndex}`]
  
    transition(elms, properties)
  }

  document.getElementById("login").onclick = function (){

    const properties = [props.left, props.opacity0, props.opacity0, `${props.transition1} ${props.bottom}`, `${props.transition2} ${props.bottom}`, `${props.opacity1} ${props.trnsDelay} ${props.zIndex}`, props.opacity0]
  
    transition(elms, properties)
  }

  document.getElementById("arrowClick").onclick = function (){

    const properties = [props.leftM120, props.opacity1, props.opacity1, props.opacity1, props.opacity1, `${props.opacity0} ${props.trnsDelay0} ${props.zIndex0}`, `${props.opacity0} ${props.trnsDelay0} ${props.zIndex0}`]
  
    transition(elms, properties)
  }
} 


function openFindIdIframe() {
	const modal = document.getElementById("findModal");
	const iframe = document.getElementById("findIdIframe");
	iframe.src = "/usr/member/findLoginId";
	modal.classList.remove("hidden");
}

function closeFindIdIframe() {
	const modal = document.getElementById("findModal");
	const iframe = document.getElementById("findIdIframe");

	modal.classList.add("hidden");
	iframe.src = "";
}

function openFindPwIframe() {
	document.getElementById("findPwModal").classList.remove("hidden");
	document.getElementById("findPwForm").src = "/usr/member/findLoginPw"; // iframe src
}

function closeFindPwIframe() {
	const modal = document.getElementById("findPwModal"); // 수정
	const iframe = document.getElementById("findPwForm");

	modal.classList.add("hidden");
	iframe.src = "";
}



$(function() {
	$('#findIdForm').on('submit', function(e) {
		e.preventDefault(); // 기본 폼 전송 막기

		const name = $('#name').val().trim();
		const email = $('#email').val().trim();

		if (name.length === 0) {
			Swal.fire({ icon: 'warning', title: '이름을 입력해주세요' });
			$('#name').focus();
			return;
		}

		if (email.length === 0) {
			Swal.fire({ icon: 'warning', title: '이메일을 입력해주세요' });
			$('#email').focus();
			return;
		}

		$.ajax({
			url: '/usr/member/doFindLoginId',
			type: 'POST',
			data: {
				name: name,
				email: email
			},
			success: function(res) {
				const getLoginId = res.data1 || "아이디 없음";

				Swal.fire({
					icon: 'success',
					title: '아이디 찾기 결과',
					text: `아이디는 [${getLoginId}] 입니다`
				});

				closeFindIdIframe();
			},
			error: function(err) {
				Swal.fire({
					icon: 'error',
					title: '요청 실패',
					text: err.responseText || '서버 오류가 발생했습니다.'
				});
			}
		});
	});
});

document.addEventListener("click", function(event) {
	const idModal = document.getElementById("findModal");
	const idContent = document.getElementById("findModalContent");

	if (idModal && idContent &&
		!idModal.classList.contains("hidden") &&
		idModal.contains(event.target) &&
		!idContent.contains(event.target)) {
		closeFindIdIframe();
	}

	const pwModal = document.getElementById("findPwModal");
	const pwContent = document.getElementById("findModalContent");

	if (pwModal && pwContent &&
		!pwModal.classList.contains("hidden") &&
		pwModal.contains(event.target) &&
		!pwContent.contains(event.target)) {
		closeFindPwIframe();
	}
});


$(function() {
	$('#findPwForm').on('submit', function(e) {
		e.preventDefault(); // ✅ 기본 제출 막기

		const loginId = $('#loginId').val()?.trim();
		const email = $('#email').val()?.trim();

		if (!loginId) {
			Swal.fire({ icon: 'warning', title: '아이디를 입력해주세요' });
			$('#loginId').focus();
			return;
		}

		if (!email) {
			Swal.fire({ icon: 'warning', title: '이메일을 입력해주세요' });
			$('#email').focus();
			return;
		}

		$.ajax({
			url: '/usr/member/doFindLoginPw',
			type: 'POST',
			data: { loginId, email },
			success: function(res) {
				if (res.resultCode?.startsWith('S-')) {
					Swal.fire({
						icon: 'success',
						title: '임시 비밀번호 발송',
						text: '메일로 임시 비밀번호를 발송했습니다.'
					});

					closeFindPwIframe();
				} else {
					Swal.fire({
						icon: 'error',
						title: '실패',
						text: res.msg || '일치하는 정보가 없습니다.'
					});
				}
			},
			error: function(err) {
				Swal.fire({
					icon: 'error',
					title: '서버 오류',
					text: err.responseText || '요청 중 문제가 발생했습니다.'
				});
			}
		});
	});
});
