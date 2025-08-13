package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    /* ========= 공통 CORS =========
       - 프런트(3001), 로컬(8080), S3 정적/객체 URL 허용
       - 크로스 오리진 쿠키 사용을 위해 AllowCredentials = true
       - * 사용 금지(정확한 오리진만)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:3001",
                "http://localhost:8080",
                "https://aniwell.s3.ap-northeast-2.amazonaws.com",
                "http://aniwell.s3-website.ap-northeast-2.amazonaws.com"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "Accept"));
        cfg.setAllowCredentials(true); // ✅ 중요: 쿠키/세션 포함 허용
        // 필요시 노출 헤더 추가
        // cfg.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg); // 전역 적용
        return source;
    }

    /* ========= 1) API 전용 체인: /api/** =========
       - 세션 사용(IF_REQUIRED) → /api/member/myPage 등에서 로그인 세션 활용 가능
       - 인증 실패 시 401만 반환(리다이렉트 없음)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // ✅ 세션 허용
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // 예: 현재 로그인 회원 조회는 인증 필요
                    .requestMatchers("/api/member/myPage").authenticated()
                    // 나머지 API는 상황에 맞게 공개/인증 설정
                    .anyRequest().permitAll()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    /* ========= 2) 앱 체인: 나머지 =========
       - 기존 정책 유지 + 일부 공개 경로 허용
     */
    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(sessionFixation -> sessionFixation.none())
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                    // 프리플라이트 허용
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 외부 접근 허용이 필요한 엔드포인트(필요 시 조정)
                    .requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()

                    // 공개 리소스
                    .requestMatchers(
                            "/",
                            "/usr/home/main",
                            "/usr/member/login", "/usr/member/doLogin",
                            "/usr/member/join", "/usr/member/doJoin",
                            "/usr/member/findLoginId", "/usr/member/findLoginPw",
                            "/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
                            "/usr/member/social-login",
                            "/usr/member/firebase-session-login",
                            "/css/**", "/js/**", "/img/**", "/img.socialLogin/**",
                            "/resource/**",
                            "/usr/member/getLoginIdDup",
                            "/usr/member/getEmailDup",
                            "/usr/member/getNicknameDup",
                            "/usr/member/getCellphoneDup"
                    ).permitAll()

                    // 그 외는 인증
                    .anyRequest().authenticated()
            )
            .formLogin(login -> login
                    .loginPage("/usr/member/login")
                    .defaultSuccessUrl("/", false)
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/usr/member/doLogout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe
                    .tokenValiditySeconds(7 * 24 * 60 * 60)
                    .alwaysRemember(true)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }
}
