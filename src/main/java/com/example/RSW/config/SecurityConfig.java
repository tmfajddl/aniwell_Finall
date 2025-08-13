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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 외부 공개 유지 필요 시
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

                        // 나머지는 로그인 필요 → 펫리스트 페이지(서버 렌더/뷰) 포함
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
       - 필요 오리진만 등록
       - 크로스 오리진에서 세션 쿠키를 쓸 경우 setAllowCredentials(true) + fetch에 credentials: 'include'
    */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "https://aniwell.s3.ap-northeast-2.amazonaws.com",
                "http://aniwell.s3-website.ap-northeast-2.amazonaws.com",
                "http://localhost:3001",
                "http://localhost:8080"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        // 크로스오리진에서 JSESSIONID 쿠키를 사용해야 하면 true 로 바꾸고, fetch에 credentials 옵션 포함
        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        source.registerCorsConfiguration("/usr/pet/daily/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }
}
