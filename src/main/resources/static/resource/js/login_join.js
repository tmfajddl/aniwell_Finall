
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
	iframe.src = ""; // 리셋
}

