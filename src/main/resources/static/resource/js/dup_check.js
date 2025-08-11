document.addEventListener("DOMContentLoaded", () => {
    console.log("✅ dup_check.js loaded");

    const joinForm = document.querySelector('.login-form--register form');
    if (!joinForm) return;

    const debounce = (func, delay) => {
        let timer;
        return (...args) => {
            clearTimeout(timer);
            timer = setTimeout(() => func.apply(this, args), delay);
        };
    };

    const checkDup = (field, inputId, warningId) => {
        const input = joinForm.querySelector(`#${inputId}`);
        const warning = joinForm.querySelector(`#${warningId}`);

        if (!input || !warning) return;

        input.addEventListener("input", () => {
            const value = input.value.trim();

            warning.textContent = "";
            warning.classList.add("hidden");
            input.classList.remove("border-red-500", "border-green-500");

            if (value === "") return;

            // 로컬 유효성 검사
            if (field === "loginId" && value.length < 6 || value.length > 12) return showWarning("아이디는 6자 이상 12자 이하로 입력해주세요.");
            if (field === "email" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return showWarning("이메일 형식이 올바르지 않습니다.");
            if (field === "nickname" && value.length < 2) return showWarning("닉네임은 2자 이상 입력해주세요.");
            if (field === "cellphone" && !/^\d{3}-\d{3,4}-\d{4}$/.test(value)) return showWarning("전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678");

            const url = `/usr/member/get${capitalize(field)}Dup?${field}=${encodeURIComponent(value)}`;

            fetch(url)
                .then(res => {
                    const ct = res.headers.get("content-type") || "";
                    if (!ct.includes("application/json")) throw new Error("JSON 아님");
                    return res.json();
                })
                .then(data => {
                    if (data.resultCode === "S-1") {
                        showSuccess(`사용 가능한 ${getFieldName(field)}입니다.`);
                    } else {
                        showWarning(`이미 사용 중인 ${getFieldName(field)}입니다.`);
                    }
                })
                .catch(err => {
                    console.error(`❌ ${field} 중복 확인 실패:`, err);
                    showWarning("서버 오류로 중복 확인에 실패했습니다.");
                });

            function showWarning(msg) {
                warning.textContent = msg;
                warning.classList.remove("hidden", "text-green-600");
                warning.classList.add("text-red-600");
                input.classList.remove("border-green-500");
                input.classList.add("border-red-500");
            }

            function showSuccess(msg) {
                warning.textContent = msg;
                warning.classList.remove("hidden", "text-red-600");
                warning.classList.add("text-green-600");
                input.classList.remove("border-red-500");
                input.classList.add("border-green-500");
            }
        });
    };


    const getFieldName = (field) => {
        switch (field) {
            case 'loginId':
                return '아이디';
            case 'email':
                return '이메일';
            case 'nickname':
                return '닉네임';
            case 'cellphone':
                return '전화번호';
            default:
                return field;
        }
    };

    const capitalize = (s) => s.charAt(0).toUpperCase() + s.slice(1);

    // 적용 필드
    checkDup('loginId', 'loginId', 'idWarning');
    checkDup('email', 'email', 'emailWarning');
    checkDup('nickname', 'nickname', 'nicknameWarning');
    checkDup('cellphone', 'cellphone', 'cellphoneWarning');
});
