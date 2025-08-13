package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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
import org.springframework.http.HttpStatus;

import java.util.List;

@Configuration
public class SecurityConfig {

    /* ========= 1) API 전용 체인: /api/** =========
       - 로그인 리다이렉트 금지(401만 반환)
       - CORS/OPTIONS 허용
       - GET/POST/DELETE 공개 (필요 시 조정)
    */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").permitAll()
                        .anyRequest().permitAll()
                )
                // 로그인 페이지로 리다이렉트하지 않고 401만 내려줌
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    /* ========= 2) 앱 체인: 나머지 =========
       - 기존 정책 유지
       - /usr/pet/daily/** 는 외부(프론트)에서 바로 호출 가능하도록 DELETE/GET 허용
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
                        // 프리플라이트 전부 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔓 외부에서 호출할 삭제/조회 엔드포인트 허용 (원하면 제거/조정 가능)
                        .requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()

                        // 기존 공개 경로
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
                                "/usr/member/getCellphoneDup"
                        ).permitAll()

                        // 나머지는 인증
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

    /* ========= 3) 공통 CORS =========
       - S3(객체 URL https / 정적사이트 http), 로컬 프런트 허용
       - 필요 시 운영/CloudFront 도메인을 여기에 추가
    */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 실제 쓰는 오리진만 남기세요 (프로토콜까지 정확히!)
        cfg.setAllowedOrigins(List.of(
                "https://aniwell.s3.ap-northeast-2.amazonaws.com",        // S3 객체 URL (HTTPS)
                "http://aniwell.s3-website.ap-northeast-2.amazonaws.com", // S3 정적 사이트 (HTTP)
                "http://localhost:3001",                                   // 프런트 dev
                "http://localhost:8080"                                    // 로컬 테스트
                // "https://your-prod-domain.com"                          // 운영/CloudFront 있으면 추가
        ));
        cfg.setAllowedMethods(List.of("GET","POST","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(false); // 쿠키/세션을 프런트에서 써야 하면 true + AllowedOrigins는 정확히 제한

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /api/** 와 /usr/pet/daily/** 둘 다 CORS 적용
        source.registerCorsConfiguration("/api/**", cfg);
        source.registerCorsConfiguration("/usr/pet/daily/**", cfg);
        // 필요시 다른 공개 경로도 추가 가능
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }
}
