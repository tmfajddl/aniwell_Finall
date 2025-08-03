
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
				Swal.fire("SweetAlert2 is working!");

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

document.addEventListener('click', function (event) {
  const modal = document.getElementById("findModal");
  const modalContent = document.getElementById("findModalContent");

  // 모달이 열려있고, 클릭한 대상이 모달 내용 영역 바깥일 경우에만 닫기
  if (!modal.classList.contains("hidden") &&
      modal.contains(event.target) &&
      !modalContent.contains(event.target)) {
    closeFindIdIframe(); // 이건 부모 함수로 존재해야 함
  }
});

