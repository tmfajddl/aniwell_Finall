
package com.example.RSW.service;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.MemberRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MemberService {

    @Value("${custom.siteMainUri}")
    private String siteMainUri;

    @Value("${custom.siteName}")
    private String siteName;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public ResultData notifyTempLoginPwByEmail(Member actor) {
        String title = "[" + siteName + "] 임시 패스워드 발송";
        String tempPassword = Ut.getTempPassword(6);
        String body = "<h1>임시 패스워드 : " + tempPassword + "</h1>";
        body += "<a href=\"" + siteMainUri + "/usr/member/login\" target=\"_blank\">로그인 하러가기</a>";

        ResultData sendResultData = mailService.send(actor.getEmail(), title, body);

        if (sendResultData.isFail()) {
            return sendResultData;
        }

        setTempPassword(actor, tempPassword);

        return ResultData.from("S-1", "계정의 이메일주소로 임시 패스워드가 발송되었습니다.");
    }

    private void setTempPassword(Member actor, String tempPassword) {
        memberRepository.modify(actor.getId(), Ut.sha256(tempPassword), null, null, null, null, null);
    }

    public ResultData<Integer> join(String loginId, String loginPw, String name, String nickname, String cellphone,
                                    String email, String address, String authName, int authLevel) {

        // 아이디 중복 체크
        Member existsMember = getMemberByLoginId(loginId);
        if (existsMember != null) {
            return ResultData.from("F-7", Ut.f("이미 사용중인 아이디(%s)입니다", loginId));
        }

        // 이름과 이메일 중복 체크
        existsMember = getMemberByNameAndEmail(name, email);
        if (existsMember != null) {
            return ResultData.from("F-8", Ut.f("이미 사용중인 이름(%s)과 이메일(%s)입니다", name, email));
        }

        // 회원가입 처리 (필수 컬럼을 테이블에 맞게 추가)
        memberRepository.doJoin(loginId, loginPw, name, nickname, cellphone, email, address, authName, authLevel);

        // 최근 삽입된 회원 ID 조회
        int id = memberRepository.getLastInsertId();

        // 성공적으로 회원가입된 후 반환
        return ResultData.from("S-1", "회원가입 성공", "가입 성공 id", id);
    }

    public Member getMemberByNameAndEmail(String name, String email) {

        return memberRepository.getMemberByNameAndEmail(name, email);

    }

    public Member getMemberByLoginId(String loginId) {

        return memberRepository.getMemberByLoginId(loginId);
    }

    public Member getMemberById(int id) {
        return memberRepository.getMemberById(id);
    }

    public ResultData modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphone,
                             String email, String photo) {

        loginPw = Ut.sha256(loginPw);

        memberRepository.modify(loginedMemberId, loginPw, name, nickname, cellphone, email, photo);

        return ResultData.from("S-1", "회원정보 수정 완료");
    }

    public ResultData modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphone,
                                      String email, String photo, String address) {
        memberRepository.modifyWithoutPw(loginedMemberId, name, nickname, cellphone, email, photo, address);

        return ResultData.from("S-1", "회원정보 수정 완료");
    }

    public ResultData withdrawMember(int id) {
        memberRepository.withdraw(id);
        return ResultData.from("S-1", "탈퇴 처리 완료");
    }


    public void updateAuthLevel(int memberId, int authLevel) {
        memberRepository.updateAuthLevel(memberId, authLevel);
    }

    public List<Member> getForPrintMembers(String searchType, String searchKeyword) {
        return memberRepository.getForPrintMembersWithCert(searchType, searchKeyword);
    }


    public void updateVetCertInfo(int memberId, String fileName, int approved) {
        memberRepository.updateVetCertInfo(memberId, fileName, approved);
    }

    public int countByAuthLevel(int level) {
        return memberRepository.countByAuthLevel(level);
    }

    // 관리자 목록을 가져오는 메서드
    public List<Member> getAdmins() {
        return memberRepository.findByAuthLevel(7); // 관리자 권한이 7인 회원들
    }

    // 소셜 로그인 시, 기존 회원 조회 또는 신규 생성
    public Member getOrCreateSocialMember(String provider, String socialId, String email, String name) {
        Member member = memberRepository.getMemberBySocial(provider, socialId);

        if (member == null) {
            // loginId 생성 (예: kakao_1234567890)
            String loginId = provider + "_" + socialId;

            // nickname은 name과 동일하게 사용
            String nickname = name;
            String loginPw = "SOCIAL_LOGIN";

            // ✅ MyBatis XML에 맞게 파라미터 6개 전달
            memberRepository.doJoinBySocial(loginId, loginPw, provider, socialId, name, nickname, email);

            int id = memberRepository.getLastInsertId();
            member = memberRepository.getMemberById(id);
        }

        return member;
    }



    public Member getOrCreateByEmail(String email, String name, String provider) {
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            String loginId = provider + "_" + email.split("@")[0];
            String loginPw = Ut.sha256("temp_pw_" + provider);
            String nickname = name;

            // provider와 socialId 구분
            memberRepository.doJoinBySocial(
                    loginId,
                    loginPw,
                    provider,
                    provider + "_" + email, // socialId = "kakao_email@noemail.kakao"
                    name,
                    nickname,
                    email
            );

            member = memberRepository.findByEmail(email);
        }

        return member;
    }

    // ✅ Firebase 커스텀 토큰 생성
    public String createFirebaseCustomToken(String uid) {
        try {
            System.out.println("📌 [DEBUG] createFirebaseCustomToken() 진입, uid = " + uid);
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            System.out.println("⚠️ FirebaseAuthException: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("❌ 기타 예외: " + e.getMessage());
            return null;
        }
    }


    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public String getOrCreateFirebaseToken(Member member) {
        String redisKey = "firebaseToken::" + member.getId();

        // 1. Redis에서 캐시 확인
        String cachedToken = redisTemplate.opsForValue().get(redisKey);
        if (cachedToken != null) {
            System.out.println("✅ [Redis] 캐시된 Firebase 토큰 반환");
            return cachedToken;
        }

        // 2. UID 확인 → 없으면 UUID 생성 + DB 저장
        String uid = member.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            uid = UUID.randomUUID().toString();
            member.setUid(uid);
            memberRepository.updateUidById(uid, member.getId());
            System.out.println("📌 [UID 생성 및 저장] " + uid);
        }

        // 3. Firebase 사용자 이메일 기반 존재 여부 확인
        try {
            // 먼저 이메일 기준 조회
            UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(member.getEmail());
            uid = existingUser.getUid();
            System.out.println("✅ [Firebase] 이메일 기반 기존 사용자 UID 확인: " + uid);

            // DB UID와 다르면 동기화
            if (!uid.equals(member.getUid())) {
                member.setUid(uid);
                memberRepository.updateUidById(uid, member.getId());
                System.out.println("🔄 [DB] UID를 Firebase UID로 동기화");
            }

        } catch (FirebaseAuthException emailEx) {
            if (emailEx.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                // 이메일도 없으면 UID 기준 조회 시도
                try {
                    FirebaseAuth.getInstance().getUser(uid);
                    System.out.println("✅ [Firebase] UID 기준 기존 사용자 확인: " + uid);
                } catch (FirebaseAuthException uidEx) {
                    if (uidEx.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                        // UID도 없으면 새 사용자 등록
                        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                                .setUid(uid)
                                .setEmail(member.getEmail())
                                .setDisplayName(member.getNickname())
                                .setEmailVerified(true);

                        try {
                            FirebaseAuth.getInstance().createUser(request);
                            System.out.println("✅ [Firebase] 새 사용자 등록 완료: " + uid);
                        } catch (FirebaseAuthException ex) {
                            throw new RuntimeException("❌ Firebase 사용자 생성 실패: " + ex.getMessage());
                        }
                    } else {
                        throw new RuntimeException("❌ Firebase UID 조회 실패: " + uidEx.getMessage());
                    }
                }
            } else {
                throw new RuntimeException("❌ Firebase 이메일 조회 실패: " + emailEx.getMessage());
            }
        }

        // 4. Custom Token 발급 (이메일 추가)
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider", member.getSocialProvider() != null ? member.getSocialProvider() : "email");
        claims.put("email", member.getEmail()); // ✅ 이메일 추가

        String customToken;
        try {
            customToken = FirebaseAuth.getInstance().createCustomToken(uid, claims);
            System.out.println("✅ [Firebase] 커스텀 토큰 발급 완료 (이메일 포함)");
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("❌ Firebase 토큰 생성 실패: " + e.getMessage());
        }

        // 5. Redis 캐싱 (TTL 1시간)
        redisTemplate.opsForValue().set(redisKey, customToken, 1, TimeUnit.HOURS);
        System.out.println("✅ [Redis] Firebase 토큰 캐싱 완료: " + redisKey);

        return customToken;
    }

    public Member findByUid(String uid) {
        return memberRepository.findByUid(uid);
    }

}
