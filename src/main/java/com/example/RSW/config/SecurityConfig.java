package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

                // ✅ 세션 재생성 방지 (OAuth 콜백 시 세션 유지)
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.none())
                )

                // ✅ iframe 허용 (동일 출처만)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // ✅ 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", // 루트 URL
                                "/usr/home/main",
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/naver/**",   // ✅ 네이버 로그인 콜백 허용
                                "/usr/member/kakao/**",   // ✅ 카카오 로그인 콜백 허용
                                "/usr/member/google/**", // ✅ 구글 로그인 콜백 허용
                                "/usr/member/social-login", // ✅ 소셜 로그인 토큰 발급 허용
                                "/usr/member/firebase-session-login", // ✅ Firebase 로그인 허용
                                "/css/**", "/js/**", "/img/**", "/img.socialLogin/**", "/resource/**"
                        ).permitAll()
                        .requestMatchers("/usr/pet/**").authenticated() // ✅ 로그인 후만 접근 가능
                        .anyRequest().authenticated()
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
                );

        return http.build();
    }



    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
