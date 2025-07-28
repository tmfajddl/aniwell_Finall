
function openComModal(contentHTML) {
	const modal = document.getElementById('comModal');
	modal.innerHTML = `
	<div class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
	  <div class="bg-white p-8 rounded-2xl shadow-xl relative w-[600px] max-w-full">
	    <!-- 닫기 버튼 -->
	    <button onclick="closeCommentModal()" class="absolute top-3 right-4 text-2xl text-gray-400 hover:text-gray-700">&times;</button>

				${contentHTML}
			</div>
		</div>
	`;
	modal.classList.remove('hidden');
}

function closeCommentModal() {
	const modal = document.getElementById("comModal");
	modal.classList.add("translate-y-full");  // 아래로 다시 내려감
}

function addPet() {
	const html = `
		<div>
		<!-- 제목 -->
		   <h2 class="text-2xl font-bold mb-6 flex items-center gap-2">
		     🐾 <span>반려동물 등록</span>
		   </h2>

		   <!-- 등록 폼 -->
		   <form action="/usr/pet/doJoin" method="post" enctype="multipart/form-data" class="space-y-6">
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