/* =========================
   auth_verify.js  (최종 통합본)
========================= */

/* ---- 전역 안전 스텁 ---- */
window.sendEmailVerificationCode = window.sendEmailVerificationCode || function () {
};
window.verifyEmailCode = window.verifyEmailCode || function () {
};
window.sendVerificationCode = window.sendVerificationCode || function () {
};
window.verifyPhoneCode = window.verifyPhoneCode || function () {
};

/* ---- 공통 유틸 ---- */
const byId = (id) => document.getElementById(id);
const show = (el) => el && el.classList.remove('hidden');
const hide = (el) => el && el.classList.add('hidden');
const setWarn = (el, msg) => {
    if (!el) return;
    el.textContent = msg || '';
    el.classList.toggle('hidden', !msg);
};

function markButtonAsDone(btn) {
    if (!btn) return;
    btn.disabled = true;
    btn.textContent = '인증완료';
    btn.classList.add('opacity-60', 'cursor-not-allowed');
}

function lockInputNoColor(el) {
    if (el) el.readOnly = true;
}

function unlockInputNoColor(el) {
    if (el) el.readOnly = false;
}

/* =========================
   이메일 인증 (회원가입)
========================= */
function initEmailVerifyForSignup() {
    const emailInput = byId('email');
    if (!emailInput) return;

    const emailSendBtn = byId('emailSendBtn');
    const emailCheckBtn = byId('emailCheckBtn');
    const emailBadge = byId('emailVerifiedTag');
    hide(emailBadge);

    let emailWarn = byId('emailWarning');
    if (!emailWarn) {
        emailWarn = document.createElement('p');
        emailWarn.id = 'emailWarning';
        emailWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        (emailSendBtn?.parentElement || emailInput).insertAdjacentElement('afterend', emailWarn);
    }

    const emailBox = byId('email-verification-box');
    const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v || '');
    let emailTxId = null;

    async function sendEmailVerificationCode() {
        if (!emailSendBtn) return;
        if (emailSendBtn.dataset.sending === '1') return;   // 중복 가드
        emailSendBtn.dataset.sending = '1';

        const email = (emailInput.value || '').trim();
        if (!isValidEmail(email)) {
            setWarn(emailWarn, '유효한 이메일 주소를 입력해주세요.');
            emailSendBtn.dataset.sending = '0';
            return;
        }
        setWarn(emailWarn, '');
        emailSendBtn.disabled = true;

        try {
            const res = await fetch('/api/verify/email/send', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email, purpose: 'signup'})
            });
            let json = null;
            try {
                json = await res.json();
            } catch (_) {
            }

            if (!res.ok || !(json?.resultCode || '').startsWith('S-')) {
                setWarn(emailWarn, json?.msg || '인증번호 전송에 실패했습니다.');
                emailSendBtn.disabled = false;
                emailSendBtn.dataset.sending = '0';
                return;
            }

            // txId 확보
            emailTxId = json?.txId
                ?? json?.data?.txId
                ?? json?.data1?.txId
                ?? (typeof json?.data1 === 'string' ? json.data1 : null);

            show(emailBox);
            setWarn(emailWarn, '인증번호를 이메일로 전송했습니다.');
            // sending 유지 → 중복 전송 방지(원하면 해제 가능)
        } catch (e) {
            setWarn(emailWarn, '네트워크 오류로 전송에 실패했습니다.');
            emailSendBtn.disabled = false;
            emailSendBtn.dataset.sending = '0';
        }
    }

    async function verifyEmailCode() {
        const code = (byId('emailVerificationCode')?.value || '').trim();
        if (!emailTxId) {
            setWarn(emailWarn, '먼저 인증번호를 요청해주세요.');
            return;
        }
        if (!code) {
            setWarn(emailWarn, '인증번호를 입력해주세요.');
            return;
        }

        try {
            const res = await fetch('/api/verify/email/check', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({txId: emailTxId, code, purpose: 'signup'})
            });
            let json = null;
            try {
                json = await res.json();
            } catch (_) {
            }

            if (!res.ok || !(json?.resultCode || '').startsWith('S-')) {
                setWarn(emailWarn, json?.msg || '인증번호가 올바르지 않습니다.');
                return;
            }

            hide(emailBox);
            lockInputNoColor(emailInput);
            markButtonAsDone(emailSendBtn);
            hide(emailBadge);
            setWarn(emailWarn, '');
            alert('이메일 인증이 완료되었습니다.');
        } catch (e) {
            setWarn(emailWarn, '네트워크 오류로 인증에 실패했습니다.');
        }
    }

    // 바인딩(중복 방지)
    if (emailSendBtn && !emailSendBtn.dataset.bound) {
        emailSendBtn.dataset.bound = '1';
        emailSendBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            clearTimeout(emailSendBtn._deb);
            emailSendBtn._deb = setTimeout(() => sendEmailVerificationCode(), 120);
        });
    }
    if (emailCheckBtn && !emailCheckBtn.dataset.bound) {
        emailCheckBtn.dataset.bound = '1';
        emailCheckBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            verifyEmailCode();
        });
    }

    // 값 변경 시 상태 초기화
    emailInput.addEventListener('input', () => {
        hide(emailBox);
        setWarn(emailWarn, '');
        unlockInputNoColor(emailInput);
        if (emailSendBtn) {
            emailSendBtn.disabled = false;
            emailSendBtn.textContent = '인증';
            emailSendBtn.classList.remove('opacity-60', 'cursor-not-allowed');
            emailSendBtn.dataset.sending = '0';
        }
        hide(emailBadge);
        emailTxId = null;
    });

    // 전역 호환
    window.sendEmailVerificationCode = sendEmailVerificationCode;
    window.verifyEmailCode = verifyEmailCode;
}

/* =========================
   전화번호(SMS) 인증 (회원가입/정보수정 공용)
   - 서버: /api/verify/sms/send, /api/verify/sms/confirm
   - 서비스 결과코드: S-OK / F-COOLDOWN(retryAfterSec)
========================= */
function initPhoneVerify() {
    const phoneInput = byId('cellphone');
    if (!phoneInput) return;

    const sendBtn = byId('phoneSendBtn');
    const verifyBtn = byId('verifyPhoneBtn') || document.querySelector('[data-role="verify-phone"]');
    const phoneBox = byId('phone-verification-box');
    const codeInput = byId('verificationCode');
    const badge = byId('phoneVerifiedTag');

    hide(badge);

    let phoneWarn = byId('phoneWarning') || byId('cellphoneWarning');
    if (!phoneWarn) {
        phoneWarn = document.createElement('p');
        phoneWarn.id = 'phoneWarning';
        phoneWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        (sendBtn?.parentElement || phoneInput).insertAdjacentElement('afterend', phoneWarn);
    }

    const normalize = v => (v || '').replace(/\D/g, '');

    // 쿨다운
    let cdTimer = null, cdRemain = 0;
    const sendBtnLabel = (sendBtn && sendBtn.textContent) ? sendBtn.textContent : '인증';

    function startCooldown(sec) {
        if (!sendBtn) return;
        clearInterval(cdTimer);
        cdRemain = Number(sec) || 60;
        sendBtn.disabled = true;
        sendBtn.textContent = `재전송(${cdRemain}s)`;
        cdTimer = setInterval(() => {
            cdRemain--;
            if (cdRemain <= 0) {
                clearInterval(cdTimer);
                sendBtn.disabled = false;
                sendBtn.textContent = sendBtnLabel;
            } else {
                sendBtn.textContent = `재전송(${cdRemain}s)`;
            }
        }, 1000);
    }

    async function sendVerificationCode() {
        if (!sendBtn) return;
        if (sendBtn.dataset.sending === '1') return;
        sendBtn.dataset.sending = '1';

        const phone = normalize(phoneInput.value);
        if (!phone) {
            setWarn(phoneWarn, '전화번호를 입력하세요.');
            sendBtn.dataset.sending = '0';
            return;
        }
        setWarn(phoneWarn, '');

        try {
            const res = await fetch('/api/verify/sms/send', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({phone})
            });
            let json = null;
            try {
                json = await res.json();
            } catch (_) {
            }

            if (res.ok && json?.resultCode === 'S-OK') {
                show(phoneBox);
                const cooldown = json?.data?.cooldownSec ?? 60;
                startCooldown(cooldown);
                setWarn(phoneWarn, '인증번호를 전송했습니다.');
                return;
            }

            if (json?.resultCode === 'F-COOLDOWN') {
                const remain = json?.data?.retryAfterSec ?? 60;
                show(phoneBox);
                startCooldown(remain);
                setWarn(phoneWarn, json?.msg || '재전송 대기 중입니다.');
                return;
            }

            setWarn(phoneWarn, json?.msg || '인증번호 전송에 실패했습니다.');
            sendBtn.dataset.sending = '0';
        } catch (e) {
            setWarn(phoneWarn, '네트워크 오류로 전송에 실패했습니다.');
            sendBtn.dataset.sending = '0';
        }
    }

    async function verifyPhoneCode() {
        const phone = normalize(phoneInput.value);
        const code = (codeInput?.value || '').trim();

        if (!phone) {
            setWarn(phoneWarn, '전화번호를 입력하세요.');
            return;
        }
        if (!code) {
            setWarn(phoneWarn, '인증코드를 입력하세요.');
            return;
        }

        try {
            const res = await fetch('/api/verify/sms/confirm', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({phone, code})
            });
            let json = null;
            try {
                json = await res.json();
            } catch (_) {
            }

            if (res.ok && json?.resultCode === 'S-OK') {
                hide(phoneBox);
                lockInputNoColor(phoneInput);
                lockInputNoColor(codeInput);
                markButtonAsDone(sendBtn);
                if (sendBtn) sendBtn.disabled = true;
                if (verifyBtn) verifyBtn.disabled = true;
                hide(phoneWarn);
                show(badge);
                alert('전화번호 인증이 완료되었습니다.');
                return;
            }

            setWarn(phoneWarn, json?.msg || '인증번호가 올바르지 않습니다.');
        } catch (e) {
            setWarn(phoneWarn, '네트워크 오류로 인증에 실패했습니다.');
        }
    }

    // 바인딩(중복 방지)
    if (sendBtn && !sendBtn.dataset.bound) {
        sendBtn.dataset.bound = '1';
        sendBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            clearTimeout(sendBtn._deb);
            sendBtn._deb = setTimeout(() => sendVerificationCode(), 120);
        });
    }
    if (verifyBtn && !verifyBtn.dataset.bound) {
        verifyBtn.dataset.bound = '1';
        verifyBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            verifyPhoneCode();
        });
    }

    // 입력 변경 시 초기화
    phoneInput.addEventListener('input', () => {
        clearInterval(cdTimer);
        if (sendBtn) {
            sendBtn.disabled = false;
            sendBtn.textContent = sendBtnLabel;
            sendBtn.dataset.sending = '0';
        }
        unlockInputNoColor(phoneInput);
        if (codeInput) {
            codeInput.readOnly = false;
            codeInput.value = '';
        }
        hide(badge);
        hide(phoneBox);
        setWarn(phoneWarn, '');
    });

    // 전역 호환
    window.sendVerificationCode = sendVerificationCode;
    window.verifyPhoneCode = verifyPhoneCode;
}

/* =========================
   init
========================= */
document.addEventListener('DOMContentLoaded', () => {
    initEmailVerifyForSignup();
    initPhoneVerify();
});
