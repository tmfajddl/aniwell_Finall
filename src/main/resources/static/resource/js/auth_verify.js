/* =========================
   auth_verify.js
   - 회원가입: 이메일 인증 (purpose='signup')
   - 전화번호: 인증 성공 시 UI 정리(입력칸 숨김/잠금)
   - 의존: 페이지에 #cellphone, #phone-verification-box, #email 가 존재
   - 서버 API: /api/verify/email/send, /api/verify/email/check
========================= */

/* ---- 공통 유틸 ---- */
const $ = (id) => document.getElementById(id);
const show = (el) => el && el.classList.remove('hidden');
const hide = (el) => el && el.classList.add('hidden');
const setWarn = (el, msg) => { if(!el) return; el.textContent = msg || ''; el.classList.toggle('hidden', !msg); };
function lockInput(inputEl, btnEl, badgeEl){
    if (inputEl) { inputEl.readOnly = true; inputEl.classList.add('bg-gray-50'); }
    if (btnEl)   { btnEl.disabled = true;  btnEl.classList.add('opacity-60','cursor-not-allowed'); }
    if (badgeEl) { show(badgeEl); }
}
function unlockInput(inputEl, btnEl, badgeEl){
    if (inputEl) { inputEl.readOnly = false; inputEl.classList.remove('bg-gray-50'); }
    if (btnEl)   { btnEl.disabled = false;  btnEl.classList.remove('opacity-60','cursor-not-allowed'); }
    if (badgeEl) { hide(badgeEl); }
}

/* =========================
   A) 전화번호: 성공 시 UI 정리
   - Firebase 인증 절차(sendVerificationCode 등)는 기존 페이지 코드 사용
   - 여기서는 verifyPhoneCode만 성공 후 UI 정리하도록 덮어씀
========================= */
(function patchPhoneVerify(){
    function ensurePhoneBadge(){
        let badge = $('phoneVerifiedTag');
        if (!badge) {
            const container = $('cellphone')?.parentElement;
            if (!container) return null;
            badge = document.createElement('span');
            badge.id = 'phoneVerifiedTag';
            badge.className = 'text-green-600 text-xs ml-2 hidden';
            badge.textContent = '인증됨';
            container.appendChild(badge);
        }
        return badge;
    }
    function getPhoneSendBtn(){
        return $('phoneSendBtn') || $('cellphone')?.parentElement?.querySelector('button.auth') || null;
    }

    // 기존 onclick="verifyPhoneCode()"를 그대로 쓰기 위해 같은 이름으로 덮어쓰기
    window.verifyPhoneCode = async function(){
        const code = $('verificationCode')?.value?.trim();
        if (!window.confirmationResult) { alert('먼저 인증번호를 요청하세요.'); return; }
        if (!code) { alert('인증번호를 입력하세요.'); return; }

        try {
            const result = await window.confirmationResult.confirm(code);

            // ✅ 성공 처리: 인증칸 숨김 + 입력/버튼 잠금 + 배지 표시
            hide($('phone-verification-box'));
            lockInput($('cellphone'), getPhoneSendBtn(), ensurePhoneBadge());
            setWarn($('cellphoneWarning'), '');

            alert('전화번호 인증이 완료되었습니다.');
        } catch (err) {
            console.error('❌ 인증 실패', err);
            alert('인증에 실패했습니다. ' + (err?.message || ''));
        }
    };

    // 값 변경 시 상태 초기화
    $('cellphone')?.addEventListener('input', () => {
        hide($('phone-verification-box')); setWarn($('cellphoneWarning'),'');
        unlockInput($('cellphone'), getPhoneSendBtn(), $('phoneVerifiedTag'));
    });
})();

/* =========================
   B) 이메일: 회원가입용 인증 (purpose='signup')
   - 아이디/비번 찾기와 같은 엔드포인트 재사용
   - HTML에 버튼/배지가 없으면 자동 주입
========================= */
(function initEmailVerifyForSignup(){
    const emailInput = $('email');
    if (!emailInput) return;

    // 1) 버튼/배지 라인 준비 (없으면 생성)
    let emailSendBtn = $('emailSendBtn');
    let emailBadge   = $('emailVerifiedTag');

    if (!emailSendBtn) {
        // input을 감싸는 줄(line)을 만들고 버튼/배지 추가
        const line = document.createElement('div');
        line.className = 'flex items-center gap-1';
        emailInput.parentNode.insertBefore(line, emailInput);
        line.appendChild(emailInput);

        emailSendBtn = document.createElement('button');
        emailSendBtn.type = 'button';
        emailSendBtn.id = 'emailSendBtn';
        emailSendBtn.className = 'auth';
        emailSendBtn.textContent = '인증';
        line.appendChild(emailSendBtn);

        emailBadge = document.createElement('span');
        emailBadge.id = 'emailVerifiedTag';
        emailBadge.className = 'text-green-600 text-xs ml-2 hidden';
        emailBadge.textContent = '인증됨';
        line.appendChild(emailBadge);
    }

    // 2) 경고 영역 준비
    let emailWarn = $('emailWarning');
    if (!emailWarn) {
        emailWarn = document.createElement('p');
        emailWarn.id = 'emailWarning';
        emailWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        emailSendBtn.parentElement.insertAdjacentElement('afterend', emailWarn);
    }

    // 3) 인증번호 입력 박스 준비 (없으면 생성)
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
    }

    // 4) 동작
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
            // 다양한 응답 포맷 대응
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

            // ✅ 성공 처리: 인증칸 숨김 + 입력/버튼 잠금 + 배지 표시
            hide(emailBox);
            lockInput(emailInput, emailSendBtn, emailBadge);
            setWarn(emailWarn, '');
            alert('이메일 인증이 완료되었습니다.');
        } catch (e) {
            setWarn(emailWarn, '네트워크 오류로 인증에 실패했습니다.');
        }
    }

    // 바인딩
    emailSendBtn.addEventListener('click', sendEmailVerificationCode);
    emailBox.querySelector('#emailCheckBtn').addEventListener('click', verifyEmailCode);

    // 값 변경 시 상태 초기화
    emailInput.addEventListener('input', ()=>{
        hide(emailBox); setWarn(emailWarn, '');
        unlockInput(emailInput, emailSendBtn, emailBadge);
        emailTxId = null;
    });

    // 필요 시 외부에서 호출할 수 있도록 노출
    window.sendEmailVerificationCode = sendEmailVerificationCode;
    window.verifyEmailCode = verifyEmailCode;
})();
