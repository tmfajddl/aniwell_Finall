package com.example.RSW.config;

import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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



    /* ========= 1) API 전용 체인: /api/** =========
       - 세션 인증 복원 허용(IF_REQUIRED) → 로그인 시 200, 비로그인 시 401
       - /api/member/**, /api/pet/** 는 인증 필요
       - 그 외 공개 API가 있으면 /api/public/** 로 두고 permitAll
       - 로그인 페이지 리다이렉트 없이 401만 반환
    */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/member/**").authenticated()
                        .requestMatchers("/api/pet/**").authenticated()
                        // 공개 API가 필요하면 ↓ 경로로 붙이세요.
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                ));
        return http.build();
    }

    /* ========= 2) 앱 체인: 그 외 전체 =========
       - 페이지 접근은 기존 정책 유지
       - /usr/pet/daily/** 만 외부에서 바로 호출 가능하도록 공개(유지)
    */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /* ✅ CORS 켜기 (아래 corsConfigurationSource() 적용) */
                .cors(Customizer.withDefaults())
                /* ✅ CSRF 비활성화 (폼로그인은 그대로 유지 가능) */


                /* 세션 (기존 동작 유지) */

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sessionFixation -> sessionFixation.none())
                )

                /* iframe 동일 출처 허용 (기존) */
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))


                /* ✅ 인가 규칙 */
                .authorizeHttpRequests(auth -> auth
                        /* 프리플라이트 전부 허용 */
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /* ✅ 공개 API: S3가 호출하는 엔드포인트 */
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        // 필요하면 POST/DELETE도 공개
                        //.requestMatchers(HttpMethod.POST, "/api/**").permitAll()
                        //.requestMatchers(HttpMethod.DELETE, "/api/**").permitAll()

                        /* (선택) 외부에서 직접 호출할 엔드포인트 있으면 유지 */
                        //.requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()


                        /* 기존 공개 경로들 */
                        .requestMatchers(
                                "/",
                                "/usr/home/main",
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/usr/member/doFindLoginId", "/usr/member/doFindLoginPw",
                                "/usr/member/findLoginId", "/usr/member/findLoginPw",
                                "/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
                                "/usr/member/social-login",
                                "/usr/member/firebase-session-login",
                                "/css/**", "/js/**", "/img/**", "/img.socialLogin/**",
                                "/resource/**",
                                "/usr/member/getLoginIdDup",
                                "/usr/member/getEmailDup",
                                "/usr/member/getNicknameDup",
                                "/usr/member/getCellphoneDup",
                                "/favicon.ico" // 파비콘 403 방지
                        ).permitAll()


                        /* 나머지는 로그인 필요 (기존) */
                        .anyRequest().authenticated()
                )

                /* 폼 로그인 (기존) */
                .formLogin(login -> login
                        .loginPage("/usr/member/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )

                /* 로그아웃 (기존) */
                .logout(logout -> logout
                        .logoutUrl("/usr/member/doLogout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                /* remember-me (기존) */
                .rememberMe(rememberMe -> rememberMe
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .alwaysRemember(true)
                );

        return http.build();
    }


    /* ✅ 공통 CORS: S3(및 필요 오리진)만 정확히 허용해서 /api/** 에 적용 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(

                "https://aniwell.s3.ap-northeast-2.amazonaws.com",        // S3 객체 URL(https)
                "http://aniwell.s3-website.ap-northeast-2.amazonaws.com", // S3 정적 사이트(http, 필요 시)
                "http://localhost:3001",                                   // 프론트 dev
                "http://localhost:8080"                                    // 로컬 테스트
                // CloudFront/운영 프런트 도메인이 있으면 여기에 추가
                // "https://www.aniwell.kr"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true); // 쿠키/세션 필요 없으면 false가 단순하고 안전

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        /* 외부에서 호출할 API 경로에만 CORS 적용 */
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }
}
