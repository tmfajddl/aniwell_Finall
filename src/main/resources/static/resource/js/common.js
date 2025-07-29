const paw = document.getElementById('cat-paw');
const btn = document.getElementById('hamburger-btn');

let isVisible = false;

btn.addEventListener('click', () => {
	isVisible = !isVisible;
	if (isVisible) {
		paw.classList.remove('left-[-100%]');
		paw.classList.add('left-0');
	} else {
		paw.classList.remove('left-0');
		paw.classList.add('left-[-100%]');
	}
});



function App() {
	const [pets, setPets] = React.useState([null])
	const [loginedMember, setLoginedMember] = React.useState(null)
	const [crew, setCrew] = React.useState(null)


	React.useEffect(() => {
		fetch(`/usr/member/myPage`)
			.then(res => res.json())
			.then((memberData) => {
				console.log("로그인 멤버:", memberData);
				setLoginedMember(memberData);
				window.localStorage.setItem("loginedMemberId", memberData.id);
			});
	}, []);

	React.useEffect(() => {
		fetch(`/api/pet/list?memberId=${loginedMember?.id}`)
			.then(res => res.json())
			.then((data) => {
				console.log(data)
				console.log("petlist: ", data.pets)
				setPets(data.pets || []); // ← 정확히 'pets'를 받아야 함
				setCrew(data.crews || []);
			});
	}, [loginedMember])
	// sidebar.js

};

let authLevel = null;
const mId = localStorage.getItem("loginedMember");
const memberPhotoDiv = document.getElementById('memberPhoto');
const defaultPhoto = "/img/default-pet.png";

function e() {
	$.ajax({
		type: "GET",
		url: `/api/member/getUsrInfo`,
		data: { memberId: mId },
		success: function(data) {
			authLevel = data.authLevel;
			if (authLevel === 7) {
				$("#adminPage").removeClass("hidden");
			}
			if (authLevel === 3) {
				$("#vetPage").removeClass("hidden");
			}
			
			const photoUrl = typeof data.photo === 'string' && data.photo.trim() !== ""
			  ? data.photo
			  : defaultPhoto;


			const img = document.createElement('img');
			img.src = photoUrl;
			img.alt = "프로필";
			img.className = "w-full h-full object-cover";

			// 이전 내용 초기화 후 삽입
			memberPhotoDiv.innerHTML = "";
			memberPhotoDiv.appendChild(img);
		},
		error: function(err) {
			console.error("getUsrInfo 실패", err);
		}
	});
}

document.querySelectorAll('.menu-item').forEach((item) => {
	e();
	item.addEventListener('click', () => {
		const page = item.dataset.page
		let url = ''
		const petId = window.localStorage.getItem('selectedPetId');
		const loginedMemberId = window.localStorage.getItem('loginedMember');
		switch (page) {
			case 'pet':
				if (!petId) {
					alert("🐾 반려동물을 등록해주세요!");
					return; // 페이지 이동 중단
				}
				url = `/usr/pet/petPage?petId=${petId}` // 로그인 ID로 교체 가능
				break
			case 'my':
				url = `/usr/pet/list?memberId=${loginedMemberId}`
				break
			case 'crew':
				url = `/usr/walkCrew/list`
				break
			case 'qna':
				url = `/usr/qna/list`
				break
			case 'admin':
				url = `/adm/article/list`
				break
			case 'vet':
				url = `/usr/vetAnswer/vetList`
				break
		}

		window.parent.location.href = url
	})
})

document.querySelectorAll('.menu-item').forEach((item) => {
	const container = item.querySelector('#cat_hand');

	if (container) {
		container.innerHTML = `
			<img src="https://res.cloudinary.com/decrm0hhf/image/upload/h_90,c_fill,q_auto,f_auto/v1752334976/cat_hand_w9zkku.png"
			     alt="고양이 발"
			     class="cat-paw w-full h-full object-contain rotate-90" />
		`;

		// 🌟 초기에는 왼쪽 바깥에 숨겨두고, group-hover 시 오른쪽으로 이동
		container.classList.add(
			"absolute", "top-[-27px]", "left-[-100px]",
			"group-hover:left-[-20px]", // ← hover 시 햄버거 위로 슬라이드
			"transition-all", "duration-500",
			"z-20", "pointer-events-none"
		);

		// 메뉴 아이템 자체에도 group 역할 부여
		item.classList.add("relative", "group");
	}
});



