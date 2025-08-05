
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

    @Autowired
    private FirebaseAuth firebaseAuth;

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

    // ✅ 소셜 로그인 시 기존 회원 조회 or 신규 생성
    public Member getOrCreateSocialMember(String provider, String socialId, String email, String name) {
        Member member = memberRepository.getMemberBySocial(provider, socialId);
        if (member != null) return member;

        member = new Member();
        member.setUid(provider + "_" + socialId);
        member.setLoginId(email != null ? email : socialId);
        member.setEmail(email);
        member.setName(name);
        member.setAuthLevel(1);
        member.setAuthName("일반회원");

        memberRepository.insert(member);
        return member;
    }

    // ✅ 이메일 기반 소셜 가입
    public Member getOrCreateByEmail(String email, String name, String provider) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            String loginId = provider + "_" + email.split("@")[0];
            String loginPw = Ut.sha256("temp_pw_" + provider);

            memberRepository.doJoinBySocial(
                    loginId,
                    loginPw,
                    provider,
                    provider + "_" + email,
                    name,
                    name,
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

    // ✅ Firebase Custom Token (Redis 캐시 활용)
    public String getOrCreateFirebaseToken(Member member) {
        String redisKey = "firebase:token:" + member.getUid();
        String cachedToken = redisTemplate.opsForValue().get(redisKey);

        if (cachedToken != null) {
            System.out.println("✅ [Redis] 캐시된 Firebase 토큰 사용");
            return cachedToken;
        }

        try {
            String customToken = firebaseAuth.createCustomToken(member.getUid());
            redisTemplate.opsForValue().set(redisKey, customToken, 12, TimeUnit.HOURS); // TTL 12시간
            return customToken;
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Firebase 토큰 생성 실패: " + e.getMessage());
        }
    }


    // ✅ UID 기반 회원 조회
    public Member findByUid(String uid) {
        return memberRepository.findByUid(uid);
    }
}
