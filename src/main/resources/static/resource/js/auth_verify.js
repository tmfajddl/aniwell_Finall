/* =========================
   auth_verify.js  (최종)
   - 회원가입: 이메일 인증 (purpose='signup')
   - 전화번호: 성공 시 버튼 텍스트만 '인증완료'로 변경 (입력칸 색 변경 X)
   - 서버 API: /api/verify/email/send, /api/verify/email/check
========================= */

/* ---- 전역 안전 스텁(덮어쓰기 않도록) ---- */
window.sendEmailVerificationCode = window.sendEmailVerificationCode || function(){};
window.verifyEmailCode          = window.verifyEmailCode          || function(){};
window.sendVerificationCode     = window.sendVerificationCode     || function(){};
window.verifyPhoneCode          = window.verifyPhoneCode          || function(){};

/* ---- 공통 유틸 ---- */
const $ = (id) => document.getElementById(id);
const show = (el) => el && el.classList.remove('hidden');
const hide = (el) => el && el.classList.add('hidden');
const setWarn = (el, msg) => { if(!el) return; el.textContent = msg || ''; el.classList.toggle('hidden', !msg); };

/* 버튼을 '인증완료' 상태로 바꾸기 (배지 미사용) */
function markButtonAsDone(btn){
    if(!btn) return;
    btn.disabled = true;
    btn.textContent = '인증완료';
    btn.classList.add('opacity-60','cursor-not-allowed');
}

/* 입력을 잠그되, 배경색은 건드리지 않음 */
function lockInputNoColor(inputEl){
    if (inputEl) inputEl.readOnly = true;
}
function unlockInputNoColor(inputEl){
    if (inputEl) inputEl.readOnly = false;
}

/* =========================
   A) 전화번호: 성공 시 UI 정리(배지 삭제, 버튼만 변경)
========================= */
(function patchPhoneVerify(){
    // 초기 렌더에서 혹시 보이는 배지는 숨김
    document.addEventListener('DOMContentLoaded', () => {
        hide($('phoneVerifiedTag'));
    });

    function phoneSendBtn(){
        return $('phoneSendBtn') || $('cellphone')?.parentElement?.querySelector('button.auth') || null;
    }

    // 기존 onclick="verifyPhoneCode()"를 유지하기 위해 같은 이름으로 정의
    window.verifyPhoneCode = async function(){
        const code = $('verificationCode')?.value?.trim();
        if (!window.confirmationResult) { alert('먼저 인증번호를 요청하세요.'); return; }
        if (!code) { alert('인증번호를 입력하세요.'); return; }

        try {
            const result = await window.confirmationResult.confirm(code);

            // ✅ 성공 처리: 인증칸 숨김 + 입력 잠금 + 버튼 '인증완료'
            hide($('phone-verification-box'));
            lockInputNoColor($('cellphone'));
            markButtonAsDone(phoneSendBtn());
            hide($('phoneVerifiedTag')); // 배지는 사용 안 함
            setWarn($('cellphoneWarning'), '');

            alert('전화번호 인증이 완료되었습니다.');
        } catch (err) {
            console.error('❌ 인증 실패', err);
            alert('인증에 실패했습니다. ' + (err?.message || ''));
        }
    };

    // 값이 바뀌면(다시 수정하면) 원상복귀
    $('cellphone')?.addEventListener('input', () => {
        hide($('phone-verification-box'));
        setWarn($('cellphoneWarning'),'');
        unlockInputNoColor($('cellphone'));
        const btn = phoneSendBtn();
        if (btn) { btn.disabled = false; btn.textContent = '인증'; btn.classList.remove('opacity-60','cursor-not-allowed'); }
        hide($('phoneVerifiedTag'));
    });
})();

/* =========================
   B) 이메일: 회원가입용 인증 (purpose='signup')
========================= */
(function initEmailVerifyForSignup(){
    const emailInput = $('email');
    if (!emailInput) return;

    // 버튼/배지/경고 영역 핸들
    let emailSendBtn = $('emailSendBtn');
    let emailBadge   = $('emailVerifiedTag'); // 배지는 쓰지 않음
    hide(emailBadge);

    let emailWarn = $('emailWarning');
    if (!emailWarn) {
        emailWarn = document.createElement('p');
        emailWarn.id = 'emailWarning';
        emailWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        (emailSendBtn?.parentElement || emailInput).insertAdjacentElement('afterend', emailWarn);
    }

    let emailBox = $('email-verification-box');
    if (!emailBox) {
        emailBox = document.createElement('div');
        emailBox.id = 'email-verification-box';
        emailBox.className = 'mt-2 hidden';
        emailBox.innerHTML = `
      <input class="form-wrapper__input w-[250px] h-[30px] mt-1" id="emailVerificationCode" type="text" placeholder="인증번호 입력"/>
      <button type="button" class="auth mt-1" id="emailCheckBtn">확인</button>
    `;
        emailWarn.insertAdjacentElement('afterend', emailBox);
    } else {
        // 확인 버튼 id 보강
        const hasIdBtn = emailBox.querySelector('#emailCheckBtn');
        if (!hasIdBtn) {
            const anyBtn = emailBox.querySelector('button.auth');
            if (anyBtn) anyBtn.id = 'emailCheckBtn';
        }
    }

    const isValidEmail = (v)=>/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v||'');
    let emailTxId = null;

    async function sendEmailVerificationCode(){
        const email = (emailInput.value||'').trim();
        if (!isValidEmail(email)) { setWarn(emailWarn, '유효한 이메일 주소를 입력해주세요.'); return; }
        setWarn(emailWarn, '');
        emailSendBtn.disabled = true;

        try {
            const res = await fetch('/api/verify/email/send', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ email, purpose:'signup' })
            });
            const json = await res.json();
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
                setWarn(emailWarn, json?.msg || '인증번호 전송에 실패했습니다.');
                emailSendBtn.disabled = false;
                return;
            }
            emailTxId = json.txId ?? json?.data?.txId ?? json?.data1?.txId ?? (typeof json?.data1==='string' ? json.data1 : null);
            show(emailBox);
            setWarn(emailWarn, '인증번호를 이메일로 전송했습니다. 메일함을 확인해주세요.');
        } catch (e) {
            setWarn(emailWarn, '네트워크 오류로 전송에 실패했습니다.');
            emailSendBtn.disabled = false;
        }
    }

    async function verifyEmailCode(){
        const code = ($('emailVerificationCode')?.value||'').trim();
        if (!emailTxId) { setWarn(emailWarn, '먼저 인증번호를 요청해주세요.'); return; }
        if (!code)      { setWarn(emailWarn, '인증번호를 입력해주세요.');   return; }

        try {
            const res = await fetch('/api/verify/email/check', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ txId: emailTxId, code, purpose:'signup' })
            });
            const json = await res.json();
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
                setWarn(emailWarn, json?.msg || '인증번호가 올바르지 않습니다.');
                return;
            }

            // ✅ 성공 처리: 인증칸 숨김 + 입력 잠금 + 버튼 '인증완료'
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

    // 바인딩
    emailSendBtn?.addEventListener('click', sendEmailVerificationCode);
    const emailCheckBtn = emailBox.querySelector('#emailCheckBtn') || emailBox.querySelector('button.auth');
    emailCheckBtn?.addEventListener('click', verifyEmailCode);

    // 값 변경 시 상태 초기화
    emailInput.addEventListener('input', ()=>{
        hide(emailBox); setWarn(emailWarn, '');
        unlockInputNoColor(emailInput);
        if (emailSendBtn) { emailSendBtn.disabled = false; emailSendBtn.textContent = '인증'; emailSendBtn.classList.remove('opacity-60','cursor-not-allowed'); }
        hide(emailBadge);
        emailTxId = null;
    });

    // 외부에서 호출할 수 있도록
    window.sendEmailVerificationCode = sendEmailVerificationCode;
    window.verifyEmailCode          = verifyEmailCode;
})();
