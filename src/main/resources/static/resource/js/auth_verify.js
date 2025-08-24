/* =========================
   auth_verify.js  (최종 수정, 충돌 해결)
========================= */

/* ---- 전역 안전 스텁 ---- */
window.sendEmailVerificationCode = window.sendEmailVerificationCode || function(){};
window.verifyEmailCode          = window.verifyEmailCode          || function(){};
window.sendVerificationCode     = window.sendVerificationCode     || function(){};
window.verifyPhoneCode          = window.verifyPhoneCode          || function(){};

/* ---- 공통 유틸 ---- */
const byId = (id) => document.getElementById(id);
const show = (el) => el && el.classList.remove('hidden');
const hide = (el) => el && el.classList.add('hidden');
const setWarn = (el, msg) => { if(!el) return; el.textContent = msg || ''; el.classList.toggle('hidden', !msg); };

function markButtonAsDone(btn){ if(!btn) return; btn.disabled = true; btn.textContent = '인증완료'; btn.classList.add('opacity-60','cursor-not-allowed'); }
function lockInputNoColor(inputEl){ if (inputEl) inputEl.readOnly = true; }
function unlockInputNoColor(inputEl){ if (inputEl) inputEl.readOnly = false; }

/* =========================
   A) 전화번호 인증 (변경 없음)
========================= */
(function patchPhoneVerify(){
    document.addEventListener('DOMContentLoaded', () => hide(byId('phoneVerifiedTag')));
    function phoneSendBtn(){ const b = byId('phoneSendBtn'); if (b) return b; const c = byId('cellphone'); return c ? c.parentElement?.querySelector('button.auth') : null; }

    window.verifyPhoneCode = async function(){
        const code = byId('verificationCode')?.value?.trim();
        if (!window.confirmationResult) { alert('먼저 인증번호를 요청하세요.'); return; }
        if (!code) { alert('인증번호를 입력하세요.'); return; }
        try {
            await window.confirmationResult.confirm(code);
            hide(byId('phone-verification-box'));
            lockInputNoColor(byId('cellphone'));
            markButtonAsDone(phoneSendBtn());
            hide(byId('phoneVerifiedTag'));
            setWarn(byId('cellphoneWarning'), '');
            alert('전화번호 인증이 완료되었습니다.');
        } catch (err) {
            console.error('❌ 인증 실패', err);
            alert('인증에 실패했습니다. ' + (err?.message || ''));
        }
    };

    const cellEl = byId('cellphone');
    if (cellEl) {
        cellEl.addEventListener('input', () => {
            hide(byId('phone-verification-box'));
            setWarn(byId('cellphoneWarning'),'');
            unlockInputNoColor(cellEl);
            const btn = phoneSendBtn();
            if (btn) { btn.disabled = false; btn.textContent = '인증'; btn.classList.remove('opacity-60','cursor-not-allowed'); }
            hide(byId('phoneVerifiedTag'));
        });
    }
})();

/* =========================
   B) 이메일 인증 (회원가입)
   👉 즉시실행(IIFE) 제거, DOMContentLoaded에서 init 호출
========================= */
function initEmailVerifyForSignup(){
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
    const isValidEmail = (v)=>/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v||'');
    let emailTxId = null;

    async function sendEmailVerificationCode(){
        if (!emailSendBtn) return;
        if (emailSendBtn.dataset.sending === '1') return;   // ✅ 중복 가드
        emailSendBtn.dataset.sending = '1';

        const email = (emailInput.value||'').trim();
        if (!isValidEmail(email)) {
            setWarn(emailWarn, '유효한 이메일 주소를 입력해주세요.');
            emailSendBtn.dataset.sending = '0';
            return;
        }
        setWarn(emailWarn, '');
        emailSendBtn.disabled = true;

        try {
            const res = await fetch('/api/verify/email/send', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ email, purpose:'signup' })
            });
            let json = null;
            try { json = await res.json(); } catch(_) {}
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
                setWarn(emailWarn, json?.msg || '인증번호 전송에 실패했습니다.');
                emailSendBtn.disabled = false;
                emailSendBtn.dataset.sending = '0';
                return;
            }
            // ✅ emailTxId 안전하게 저장 (모든 경우 커버)
            emailTxId = json?.txId
                ?? json?.data?.txId
                ?? json?.data1?.txId
                ?? (typeof json?.data1 === 'string' ? json.data1 : null);

            show(emailBox);
            setWarn(emailWarn, '인증번호를 이메일로 전송했습니다.');

            // 성공 후에는 다음 단계(확인)로 진행하므로 sending 유지해도 중복 전송 방지됨
        } catch (e) {
            setWarn(emailWarn, '네트워크 오류로 전송에 실패했습니다.');
            emailSendBtn.disabled = false;
            emailSendBtn.dataset.sending = '0';
        }
    }

    async function verifyEmailCode(){
        const code = (byId('emailVerificationCode')?.value||'').trim();
        if (!emailTxId) { setWarn(emailWarn, '먼저 인증번호를 요청해주세요.'); return; }
        if (!code)      { setWarn(emailWarn, '인증번호를 입력해주세요.');   return; }

        try {
            const res = await fetch('/api/verify/email/check', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ txId: emailTxId, code, purpose:'signup' })
            });
            let json = null;
            try { json = await res.json(); } catch(_) {}
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
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

    // ✅ 바인딩: 중복 방지
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
    emailInput.addEventListener('input', ()=>{
        hide(emailBox); setWarn(emailWarn, '');
        unlockInputNoColor(emailInput);
        if (emailSendBtn) {
            emailSendBtn.disabled = false;
            emailSendBtn.textContent = '인증';
            emailSendBtn.classList.remove('opacity-60','cursor-not-allowed');
            emailSendBtn.dataset.sending = '0';
        }
        hide(emailBadge);
        emailTxId = null;
    });

    // 전역 호환 유지
    window.sendEmailVerificationCode = sendEmailVerificationCode;
    window.verifyEmailCode           = verifyEmailCode;
}

// ✅ defer 없이: DOM 로드 후 init
document.addEventListener('DOMContentLoaded', initEmailVerifyForSignup);
