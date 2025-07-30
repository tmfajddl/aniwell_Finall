package com.example.RSW.service;

import com.example.RSW.vo.Member;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseService {

    /**
     * Firebase Custom Token 생성
     * @param member 로그인된 회원 객체
     * @return Firebase Custom Token 문자열
     */
    public String createCustomToken(Member member) {
        try {
            // UID 생성 (member.uid가 없으면 ID 기반 생성)
            String uid = (member.getUid() != null && !member.getUid().isEmpty())
                    ? member.getUid()
                    : "member-" + member.getId();

            // 이메일 클레임 추가
            Map<String, Object> additionalClaims = new HashMap<>();
            if (member.getEmail() != null && !member.getEmail().isEmpty()) {
                additionalClaims.put("email", member.getEmail());
            }

            // Firebase Custom Token 생성 (이메일 포함)
            return FirebaseAuth.getInstance().createCustomToken(uid, additionalClaims);

        } catch (FirebaseAuthException e) {
            throw new RuntimeException("❌ Firebase Custom Token 생성 실패", e);
        }
    }
}
