
function openComModal(contentHTML) {
	const modal = document.getElementById('comModal');

	modal.innerHTML = `
    <div onclick="closeCommentModal()" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
      <div onclick="event.stopPropagation()" class="bg-white p-8 rounded-2xl shadow-xl relative w-[600px] max-w-full">
        <!-- 닫기 버튼 -->
        <button id="closeModalBtn" class="absolute top-3 right-4 text-2xl text-gray-400 hover:text-gray-700">&times;</button>
        ${contentHTML}
      </div>
    </div>
  `;

	modal.classList.remove("hidden");
	modal.classList.remove("translate-y-full");

	// 이벤트 핸들러도 다시 바인딩
	document.getElementById("closeModalBtn").addEventListener("click", closeCommentModal);
}


function closeCommentModal() {
	const modal = document.getElementById("comModal");

	// 이동 효과 제거
	modal.classList.add("translate-y-full");
	// ⭐ 살짝 delay 후 hidden 처리
	setTimeout(() => {
		modal.classList.add("hidden");
		modal.innerHTML = ''; // 내용도 제거 (선택)
	}, 300); // 애니메이션 시간에 맞게 설정 (Tailwind 기본은 300ms)
}

function addPet() {
	const html = `
		<div>
		<!-- 제목 -->
		   <h2 class="text-2xl font-bold mb-6 flex items-center gap-2">
		     🐾 <span>반려동물 등록</span>
		   </h2>

		   <!-- 등록 폼 -->
		   <form id="addPetForm" onsubmit="submitPetForm(e)" action="/usr/pet/doJoin" method="post" enctype="multipart/form-data" class="space-y-6">
		     <div class="flex gap-6">
		       <!-- 🐶 사진 업로드 -->
		       <div class="flex flex-col items-center space-y-3">
		         <img id="photo-preview" src="/img/default-pet.png" alt="사진" class="w-40 h-40 rounded-full object-cover border border-gray-300" />
		         <label for="photo" class="cursor-pointer text-sm text-gray-600 hover:underline">📷 파일 선택</label>
		         <input type="file" id="photo" name="photo" accept="image/*" onchange="previewPhoto(this)" class="hidden" />
		       </div>

		       <!-- 📋 정보 입력 -->
		       <div class="flex-1 grid grid-cols-2 gap-4">
		         <div class="col-span-2">
		           <label class="block text-sm font-medium mb-1" for="name">이름</label>
		           <input type="text" id="name" name="name" required class="w-full border rounded px-3 py-2" />
		         </div>

		         <div>
		           <label class="block text-sm font-medium mb-1" for="species">종</label>
		           <select id="species" name="species" required class="w-full border rounded px-3 py-2">
		             <option value="">선택</option>
		             <option value="강아지">강아지</option>
		             <option value="고양이">고양이</option>
		           </select>
		         </div>

		         <div>
		           <label class="block text-sm font-medium mb-1" for="breed">품종</label>
		           <input type="text" id="breed" name="breed" required class="w-full border rounded px-3 py-2" />
		         </div>

		         <div>
		           <label class="block text-sm font-medium mb-1" for="gender">성별</label>
		           <select id="gender" name="gender" required class="w-full border rounded px-3 py-2">
		             <option value="">선택</option>
		             <option value="수컷">수컷</option>
		             <option value="암컷">암컷</option>
		           </select>
		         </div>

		         <div>
		           <label class="block text-sm font-medium mb-1" for="birthDate">생일</label>
		           <input type="date" id="birthDate" name="birthDate" required class="w-full border rounded px-3 py-2" />
		         </div>

		         <div class="col-span-2">
		           <label class="block text-sm font-medium mb-1" for="weight">체중 (kg)</label>
		           <input type="number" step="0.1" id="weight" name="weight" required class="w-full border rounded px-3 py-2" />
		         </div>
		       </div>
		     </div>

		     <!-- 등록 버튼 -->
		     <div class="text-center">
		       <button type="submit" class="bg-yellow-400 hover:bg-yellow-500 text-white font-semibold px-6 py-2 rounded shadow">
		         등록 완료
		       </button>
		     </div>
		   </form>
		</div>
		`;
	openComModal(html);
}


function modifyPet(pet) {

	const html = `
    <div>
      <h2 class="text-2xl font-bold mb-6 flex items-center gap-2">
        🐾 <span>반려동물 정보 수정</span>
      </h2>

      <form id="modifyPetForm" onsubmit="submitModifyForm(e)" action="/usr/pet/doModify" method="post" enctype="multipart/form-data" class="space-y-6">
        <input type="hidden" name="petId" value="${pet.id}" />

        <div class="flex gap-6">
          <div class="flex flex-col items-center space-y-3">
            <img id="photo-preview" src="${pet.photo || '/img/default-pet.png'}" alt="사진" class="w-40 h-40 rounded-full object-cover border border-gray-300" />
            <label for="photo" class="cursor-pointer text-sm text-gray-600 hover:underline">📷 파일 선택</label>
            <input type="file" id="photo" name="photo" accept="image/*" onchange="previewPhoto(this)" class="hidden" />
          </div>

          <div class="flex-1 grid grid-cols-2 gap-4">
            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1" for="name">이름</label>
              <input type="text" id="name" name="name" value="${pet.name}" required class="w-full border rounded px-3 py-2" />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1" for="species">종</label>
              <select id="species" name="species" required class="w-full border rounded px-3 py-2">
                <option value="강아지" ${pet.species === '강아지' ? 'selected' : ''}>강아지</option>
                <option value="고양이" ${pet.species === '고양이' ? 'selected' : ''}>고양이</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1" for="breed">품종</label>
              <input type="text" id="breed" name="breed" value="${pet.breed}" required class="w-full border rounded px-3 py-2" />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1" for="gender">성별</label>
              <select id="gender" name="gender" required class="w-full border rounded px-3 py-2">
                <option value="수컷" ${pet.gender === '수컷' ? 'selected' : ''}>수컷</option>
                <option value="암컷" ${pet.gender === '암컷' ? 'selected' : ''}>암컷</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1" for="birthDate">생일</label>
              <input type="date" id="birthDate" name="birthDate" value="${pet.birthDate}" required class="w-full border rounded px-3 py-2" />
            </div>

            <div class="col-span-2">
              <label class="block text-sm font-medium mb-1" for="weight">체중 (kg)</label>
              <input type="number" step="0.1" id="weight" name="weight" value="${pet.weight}" required class="w-full border rounded px-3 py-2" />
            </div>
          </div>
        </div>

        <div class="text-center">
          <button type="submit" class="bg-yellow-400 hover:bg-yellow-500 text-white font-semibold px-6 py-2 rounded shadow">
            수정 완료
          </button>
        </div>
      </form>
    </div>
  `;
	openComModal(html);

}

function submitModifyForm(e) {
	e.preventDefault();

	const form = document.getElementById('modifyPetForm');
	const formData = new FormData(form);

	fetch('/usr/pet/doModify', {
		method: 'POST',
		body: formData
	})
		.then(res => res.json())
		.then(data => {
			if (data.resultCode?.startsWith("S-")) {
				closeCommentModal(); // 모달 닫기
			} else {
				alert("수정실패에요!");
			}
		})
		.catch(err => {
			console.error("❌ 수정 중 오류:", err);
		
		});
}

