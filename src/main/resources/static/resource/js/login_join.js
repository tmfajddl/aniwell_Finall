
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
