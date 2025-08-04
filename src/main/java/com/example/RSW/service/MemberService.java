
package com.example.RSW.service;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        String encodedPw = passwordEncoder.encode(tempPassword);
        memberRepository.modify(actor.getId(), encodedPw, null, null, null, null, null);
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

        // ✅ 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(loginPw);

        // 회원가입 처리 (필수 컬럼을 테이블에 맞게 추가)
        memberRepository.doJoin(loginId, encodedPw, loginPw, name, nickname, cellphone, email, address, authName, authLevel);

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

        if (loginPw != null && !loginPw.trim().isEmpty()) {
            loginPw = passwordEncoder.encode(loginPw);
        }
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
        // 1️⃣ provider + socialId 기반 조회
        Member member = memberRepository.getMemberBySocial(provider, socialId);
        if (member != null) return member;

        // 2️⃣ 이메일 중복 시 소셜 정보 업데이트
        Member emailMember = memberRepository.findByEmail(email);
        if (emailMember != null) {
            emailMember.setSocialProvider(provider);
            emailMember.setSocialId(socialId);
            memberRepository.updateSocialInfo(emailMember);
            return emailMember;
        }

        // 3️⃣ 신규 가입
        String loginId = provider + "_" + socialId;
        String loginPw = "SOCIAL_LOGIN";
        memberRepository.doJoinBySocial(loginId, loginPw, provider, socialId, name, name, email);

        int id = memberRepository.getLastInsertId();
        return memberRepository.getMemberById(id);
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
        String redisKey = "firebaseToken::" + uid;
        String cachedToken = redisTemplate.opsForValue().get(redisKey);

        if (cachedToken != null) return cachedToken;

        try {
            String token = FirebaseAuth.getInstance().createCustomToken(uid);
            redisTemplate.opsForValue().set(redisKey, token, 1, TimeUnit.HOURS);
            return token;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("❌ Firebase 토큰 생성 실패: " + e.getMessage());
        }
    }


    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public String getOrCreateFirebaseToken(Member member) {
        String redisKey = "firebaseToken::" + member.getId();

        // 1. Redis 캐시 확인 (6시간 TTL)
        String cachedToken = redisTemplate.opsForValue().get(redisKey);
        if (cachedToken != null) {
            System.out.println("✅ [Redis] 캐시된 Firebase 토큰 반환");
            return cachedToken;
        }

        // 2. UID 확인 → 없으면 예외
        String uid = member.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            throw new RuntimeException("UID가 없습니다. 회원가입 시 UID를 생성하세요.");
        }

        // 3. Firebase UID 사용자 확인 (try-catch로 감싸기)
        try {
            FirebaseAuth.getInstance().getUser(uid); // UID 기반 사용자 조회
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                try {
                    // 사용자 없으면 새로 생성
                    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                            .setUid(uid)
                            .setEmail(member.getEmail())
                            .setDisplayName(member.getNickname())
                            .setEmailVerified(true);
                    FirebaseAuth.getInstance().createUser(request);
                    System.out.println("✅ Firebase 신규 사용자 생성 완료");
                } catch (FirebaseAuthException createEx) {
                    System.err.println("❌ Firebase 사용자 생성 실패: " + createEx.getMessage());
                    throw new RuntimeException("Firebase 사용자 생성 실패", createEx);
                }
            } else {
                System.err.println("❌ Firebase UID 조회 실패: " + e.getMessage());
                throw new RuntimeException("Firebase UID 조회 실패", e);
            }
        }

        // 4. 커스텀 토큰 발급
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", member.getEmail());
            claims.put("provider", member.getSocialProvider() != null ? member.getSocialProvider() : "email");

            String customToken = FirebaseAuth.getInstance().createCustomToken(uid, claims);
            redisTemplate.opsForValue().set(redisKey, customToken, 6, TimeUnit.HOURS);
            System.out.println("✅ Firebase 토큰 발급 및 Redis 캐싱 완료");
            return customToken;
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Firebase 토큰 생성 실패: " + e.getMessage());
            throw new RuntimeException("Firebase 토큰 생성 실패", e);
        }
    }


    public Member findByUid(String uid) {
        if (uid.contains("_")) {
            String[] parts = uid.split("_", 2);
            return memberRepository.getMemberBySocial(parts[0], parts[1]);
        }
        return null;
    }
}
