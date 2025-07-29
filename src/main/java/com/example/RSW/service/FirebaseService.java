package com.example.RSW.service;

import com.example.RSW.vo.Member;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    /**
     * Firebase Custom Token 생성
     * @param member 로그인된 회원 객체
     * @return Firebase Custom Token 문자열
     */
    public String createCustomToken(Member member) {
        try {
            // UID는 member 테이블의 uid 컬럼을 사용 (없다면 ID 기반 생성 가능)
            String uid = (member.getUid() != null && !member.getUid().isEmpty())
                    ? member.getUid()
                    : "member-" + member.getId();

            // Firebase Custom Token 생성
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("❌ Firebase Custom Token 생성 실패", e);
        }
    }
}
