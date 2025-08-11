document.addEventListener("DOMContentLoaded", () => {
    console.log("✅ dup_check.js loaded");

    const joinForm = document.querySelector('.login-form--register form');
    if (!joinForm) return;

    // 한글 입력(IME 조합/붙여넣기 포함) 차단
    function preventKoreanInput(input) {
        let isComposing = false;

        input.addEventListener("compositionstart", () => {
            isComposing = true;
        });

        input.addEventListener("compositionend", (e) => {
            isComposing = false;
            if (e && e.data && /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(e.data)) {
                input.value = input.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, "");
            }
        });

        input.addEventListener("input", () => {
            if (!isComposing) {
                input.value = input.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, "");
            }
        });

        input.addEventListener("keydown", (e) => {
            if (/^[ㄱ-ㅎㅏ-ㅣ가-힣]$/.test(e.key)) {
                e.preventDefault();
            }
        });

        input.addEventListener("beforeinput", (e) => {
            if (e.data && /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(e.data)) {
                e.preventDefault();
            }
        });

        input.addEventListener("paste", (e) => {
            const pasteData = (e.clipboardData || window.clipboardData).getData('text');
            if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(pasteData)) {
                e.preventDefault();
            }
        });
    }

    // 디바운스
    const debounce = (func, delay = 300) => {
        let timer;
        return (...args) => {
            clearTimeout(timer);
            timer = setTimeout(() => func.apply(null, args), delay);
        };
    };

    // 경고/성공 메시지
    function showWarningFactory(input, warning) {
        return function showWarning(msg) {
            warning.textContent = msg;
            warning.classList.remove("hidden", "text-green-600");
            warning.classList.add("text-red-600");
            input.classList.remove("border-green-500");
            input.classList.add("border-red-500");
        };
    }

    function showSuccessFactory(input, warning) {
        return function showSuccess(msg) {
            warning.textContent = msg;
            warning.classList.remove("hidden", "text-red-600");
            warning.classList.add("text-green-600");
            input.classList.remove("border-red-500");
            input.classList.add("border-green-500");
        };
    }

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

    // 중복 체크 바인딩
    const checkDup = (field, inputId, warningId) => {
        const input = joinForm.querySelector(`#${inputId}`);
        const warning = joinForm.querySelector(`#${warningId}`);
        if (!input || !warning) return;

        const showWarning = showWarningFactory(input, warning);
        const showSuccess = showSuccessFactory(input, warning);

        // 아이디 필드엔 한글 입력 방지
        if (field === "loginId") preventKoreanInput(input);

        const handler = () => {
            const value = input.value.trim();

            // 메시지/스타일 초기화
            warning.textContent = "";
            warning.classList.add("hidden");
            warning.classList.remove("text-red-600", "text-green-600");
            input.classList.remove("border-red-500", "border-green-500");

            if (value === "") return;

            // 로컬 유효성 플래그
            let localInvalid = false;

            if (field === "loginId") {
                if (!/^[A-Za-z]/.test(value)) {
                    showWarning("아이디는 영문자로 시작해야 합니다.");
                    localInvalid = true;
                } else if (value.length < 6 || value.length > 12) {
                    showWarning("아이디는 6자 이상 12자 이하로 입력해주세요.");
                    localInvalid = true;
                }
            }

            if (field === "email") {
                const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
                if (!emailPattern.test(value)) {
                    return showWarning("이메일 형식이 올바르지 않습니다.");
                }
            }

            if (field === "nickname" && value.length < 2) {
                return showWarning("닉네임은 2자 이상 입력해주세요.");
            }

            if (field === "cellphone") {
                const digitsOnly = value.replace(/\D/g, "");                // 010, 011, 016, 017, 018, 019 허용
                // 010 → 11자리, 그 외 01X(1,6,7,8,9) → 10자리
                const isValid = /^(010\d{8}|01[16789]\d{7})$/.test(digitsOnly);

                if (!isValid) {
                    return showWarning("전화번호 형식이 올바르지 않습니다.");
                }
            }

            const url = `/usr/member/get${capitalize(field)}Dup?${field}=${encodeURIComponent(value)}`;

            // 지연 응답 무시용 스냅샷
            const snapshot = value;

            fetch(url)
                .then(res => {
                    const ct = res.headers.get("content-type") || "";
                    if (!ct.includes("application/json")) throw new Error("JSON 아님");
                    return res.json();
                })
                .then(data => {
                    if (input.value.trim() !== snapshot) return; // 입력 변경됨 → 무시

                    if (data.resultCode === "S-1") {
                        if (!localInvalid) {
                            showSuccess(`사용 가능한 ${getFieldName(field)}입니다.`);
                        }
                    } else {
                        showWarning(`이미 사용 중인 ${getFieldName(field)}입니다.`);
                    }
                })
                .catch(err => {
                    console.error(`❌ ${field} 중복 확인 실패:`, err);
                    showWarning("서버 오류로 중복 확인에 실패했습니다.");
                });
        };

        // // 디바운스로 서버 호출 줄이기
        // input.addEventListener("input", debounce(handler, 100));

        // 즉시 반응
        input.addEventListener("input", handler);
    };

    // 적용 필드
    checkDup('loginId', 'loginId', 'idWarning');
    checkDup('email', 'email', 'emailWarning');
    checkDup('nickname', 'nickname', 'nicknameWarning');
    checkDup('cellphone', 'cellphone', 'cellphoneWarning');
});
