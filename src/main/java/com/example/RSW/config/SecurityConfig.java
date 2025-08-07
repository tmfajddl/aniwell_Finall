package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // ✅ 세션 재생성 방지 (OAuth 콜백 시 세션 유지) + 세션 타임아웃 7일
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 필요 시 세션 생성 및 유지
                        .sessionFixation(sessionFixation -> sessionFixation.none()) // OAuth 콜백 시 세션 유지
                )

                // ✅ iframe 허용 (동일 출처만)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // ✅ 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/usr/home/main",
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/findLoginId", "/usr/member/findLoginPw",
                                "/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
                                "/usr/member/social-login",
                                "/usr/member/firebase-session-login",
                                "/css/**", "/js/**", "/img/**", "/img.socialLogin/**", "/resource/**",
                                "/usr/member/getLoginIdDup",
                                "/usr/member/getEmailDup",
                                "/usr/member/getNicknameDup",
                                "/usr/member/getCellphoneDup"
                        ).permitAll()
                        .anyRequest().authenticated()  // ✅ 그 외는 로그인 필요
                )


                // ✅ 폼 로그인 설정
                .formLogin(login -> login
                        .loginPage("/usr/member/login")
                        .defaultSuccessUrl("/", false) // 로그인 성공 후 이전 URL 유지
                        .permitAll()
                )

                // ✅ 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/usr/member/doLogout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ✅ remember-me (7일 유지)
                .rememberMe(rememberMe -> rememberMe
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일 유지
                        .alwaysRemember(true)
                );

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);  // 기본 10 → 8로 낮춰 인증 속도 개선
    }
}
