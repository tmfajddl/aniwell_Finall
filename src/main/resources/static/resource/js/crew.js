
function openModal(contentHTML) {
	const modal = document.getElementById('modal');
	const content = document.getElementById('modalContent');

	if (!content) {
		console.error("모달 컨텐츠 요소가 없습니다.");
		return;
	}
	content.innerHTML = `
			${contentHTML}
		`;

	modal.classList.remove('hidden');
}

function closeModal() {
	document.getElementById('modal').classList.add('hidden');
}

// 📸 사진 추가 모달
function photoModal() {
	const html = `
	<div class="flex h-full">
	  <!-- 왼쪽 이미지 영역 -->
	  <div class="w-1/2 bg-gray-100">
	    <img src="https://via.placeholder.com/500" alt="product" class="object-cover w-full h-full" />
	  </div>

	  <!-- 오른쪽 텍스트 영역 -->
	  <div class="w-1/2 p-6 flex flex-col justify-between text-gray-800 space-y-4 relative">
	    <!-- 게시글 본문 -->
	    <div class="flex-1 flex flex-col justify-between shadow p-4 overflow-auto">
	      <div class="overflow-y-auto h-[300px] text-sm leading-relaxed mb-4">
	        <p>게시글</p>
	      </div>
	      <div class="flex justify-between text-xs text-gray-500 mt-2">
	        <span class="font-bold">admin</span>
	        <span>2025.07.20</span>
	      </div>
	    </div>

	    <!-- 댓글 버튼 -->
	    <div class="shadow p-4 text-sm rounded cursor-pointer hover:bg-gray-100" onclick="openCommentModal()">
	      <p class="text-gray-500">여기누르기기</p>
	    </div>

	    <!-- ✅ 오른쪽 영역 내부에서 슬라이드되는 댓글 모달 -->
	    <div id="commentModal"
	         class="absolute bottom-0 left-0 w-full bg-white		ease-in-out
		            shadow-[0_-4px_10px_rgba(0,0,0,0.1)] rounded-t-2xl p-4 z-50 transform translate-y-full transition-transform duration-300 ease-in-out">
	      <div class="flex justify-between items-center mb-2">
	        <h2 class="text-lg font-semibold">댓글</h2>
	        <button onclick="closeCommentModal()" class="text-gray-500 hover:text-black text-sm">닫기 ✕</button>
	      </div>

	      <div class="overflow-y-auto max-h-60 space-y-2">
	        <div class="text-sm border-b pb-2">닉네임1: 123123</div>
	        <div class="text-sm border-b pb-2">닉네임2: 123123123</div>
	      </div>

	      <div class="mt-4 flex gap-2">
	        <input type="text" placeholder="댓글 입력..." class="flex-1 border px-3 py-2 rounded-md text-sm" />
	        <button class="bg-green-200 px-4 py-2 rounded-md text-sm">작성</button>
	      </div>
	    </div>
	  </div>
	</div>

    `;
	openModal(html);
}

function openCommentModal() {
	const modal = document.getElementById("commentModal");
	modal.classList.remove("translate-y-full");  // 위로 올라오게
}

function closeCommentModal() {
	const modal = document.getElementById("commentModal");
	modal.classList.add("translate-y-full");  // 아래로 다시 내려감
}

// 📝 게시글 작성 모달
function articleModal() {
	const html = `
	<div class="flex h-full">
		  <!-- 왼쪽 이미지 영역 -->
		  <div class="w-1/2 bg-gray-100">
		    <img src="https://via.placeholder.com/500" alt="product" class="object-cover w-full h-full" />
		  </div>

		  <!-- 오른쪽 텍스트 영역 -->
		  <div class="w-1/2 p-6 flex flex-col justify-between text-gray-800 space-y-4 relative">
		    <!-- 게시글 본문 -->
		    <div class="flex-1 flex flex-col justify-between shadow p-4 overflow-auto">
		      <div class="overflow-y-auto h-[300px] text-sm leading-relaxed mb-4">
		        <p>게시글</p>
		      </div>
		      <div class="flex justify-between text-xs text-gray-500 mt-2">
		        <span class="font-bold">admin</span>
		        <span>2025.07.20</span>
		      </div>
		    </div>

		    <!-- 댓글 버튼 -->
		    <div class="shadow p-4 text-sm rounded cursor-pointer hover:bg-gray-100" onclick="openCommentModal()">
		      <p class="text-gray-500">여기누르기기</p>
		    </div>

		    <!-- ✅ 오른쪽 영역 내부에서 슬라이드되는 댓글 모달 -->
		    <div id="commentModal"
		         class="absolute bottom-0 left-0 w-full bg-white		ease-in-out
			            shadow-[0_-4px_10px_rgba(0,0,0,0.1)] rounded-t-2xl p-4 z-50 transform translate-y-full transition-transform duration-300 ease-in-out">
		      <div class="flex justify-between items-center mb-2">
		        <h2 class="text-lg font-semibold">댓글</h2>
		        <button onclick="closeCommentModal()" class="text-gray-500 hover:text-black text-sm">닫기 ✕</button>
		      </div>

		      <div class="overflow-y-auto max-h-60 space-y-2">
		        <div class="text-sm border-b pb-2">닉네임1: 123123</div>
		        <div class="text-sm border-b pb-2">닉네임2: 123123123</div>
		      </div>

		      <div class="mt-4 flex gap-2">
		        <input type="text" placeholder="댓글 입력..." class="flex-1 border px-3 py-2 rounded-md text-sm" />
		        <button class="bg-green-200 px-4 py-2 rounded-md text-sm">작성</button>
		      </div>
		    </div>
		  </div>
		</div>
    `;
	openModal(html);
}


function openModal(contentHTML) {
	const modal = document.getElementById('modal');
	const content = document.getElementById('modalContent');

	if (!content) {
		console.error("모달 컨텐츠 요소가 없습니다.");
		return;
	}

	content.innerHTML = `
		${contentHTML}
	`;

	modal.classList.remove('hidden');
}

function openComModal(contentHTML) {
	const modal = document.getElementById('comModal');
	modal.innerHTML = `
		<div class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
			<div class="bg-white p-6 rounded-lg shadow-lg relative max-w-md w-full">
				<button onclick="closeComModal()" class="absolute top-2 right-4 text-xl text-gray-500 hover:text-black">&times;</button>
				${contentHTML}
			</div>
		</div>
	`;
	modal.classList.remove('hidden');
}

function closeModal() {
	document.getElementById('modal').classList.add('hidden');
}

function closeComModal() {
	document.getElementById('comModal').classList.add('hidden');
	document.getElementById('comModal').innerHTML = ''; // 내용도 초기화
}

function memberModal() {
	const html = `
		<h2 class="text-lg font-bold mb-4">멤버 정보</h2>
		<div class="flex items-center gap-4">
			<div class="w-16 h-16 bg-gray-300 rounded-full"></div>
			<div>
				<p class="font-semibold">닉네임</p>
				<p class="text-sm text-gray-500">간단한 소개</p>
			</div>
		</div>
	`;
	openComModal(html);
}

// 📅 일정 보기 모달
function scModal() {
	const html = `
      <h2 class="text-lg font-bold mb-4">일정 정보</h2>
      <p>7월 7일</p>
      <p class="text-sm text-gray-500">오후 2시 / 장소: OO공원</p>
      <button class="mt-4 px-4 py-2 bg-blue-300 rounded">참석하기</button>
    `;
	openComModal(html);
}

