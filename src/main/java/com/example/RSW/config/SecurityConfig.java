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
	/*
	 * ========= 1) API 전용 체인 (/api/**) ========= - CORS 활성화 - CSRF 완전 비활성화(프론트 호출
	 * 편의) - 인증 필요 경로만 명시, 나머지 공개 - 페이지 리다이렉트 없이 401/403만 내려줌
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**").cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/public/**").permitAll()
						// 여기는 인증 필요로 유지
						.requestMatchers("/api/member/**").authenticated().requestMatchers("/api/pet/**")
						.authenticated()
						// 그 외 API는 공개
						.anyRequest().permitAll())
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 401
				);
		return http.build();
	}

	/*
	 * ========= 2) 앱 전체 체인 (그 외 전부) ========= - CORS: /usr/** 까지 적용
	 * (S3/CloudFront에서 호출 시 403 방지) - CSRF: /usr/** 는 토큰 검사 제외(ajax POST 403 방지) -
	 * 공개 페이지/리소스 명확화 - 나머지는 로그인 필요
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf
				// ajax로 많이 쓰는 /usr/** 전체는 CSRF 제외
				.ignoringRequestMatchers("/usr/**"))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation(sessionFixation -> sessionFixation.none()))
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						/* 정적/공개 리소스 */
						.requestMatchers("/", "/usr/home/main", "/usr/member/login", "/usr/member/doLogin",
								"/usr/member/join", "/usr/member/doJoin", "/usr/member/doFindLoginId",
								"/usr/member/doFindLoginPw", "/usr/member/findLoginId", "/usr/member/findLoginPw",
								"/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
								"/usr/member/social-login", "/usr/member/firebase-session-login",
								"/usr/member/getLoginIdDup", "/usr/member/getEmailDup", "/usr/member/getNicknameDup",
								"/usr/member/getCellphoneDup", "/css/**", "/js/**", "/img/**", "/img.socialLogin/**",
								"/resource/**", "/favicon.ico")
						.permitAll()
						/* 외부에서 직접 호출할 경로(요구사항 그대로 공개 유지) */
						.requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
						/* 나머지는 로그인 필요 */
						.anyRequest().authenticated())
				.formLogin(login -> login.loginPage("/usr/member/login").defaultSuccessUrl("/", false).permitAll())
				.logout(logout -> logout.logoutUrl("/usr/member/doLogout").logoutSuccessUrl("/")
						.invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
				.rememberMe(rememberMe -> rememberMe.tokenValiditySeconds(7 * 24 * 60 * 60).alwaysRemember(true));
		return http.build();
	}

	/*
	 * ========= 공통 CORS ========= - 실제 호출 오리진을 정확히 허용 - /api/** 뿐 아니라 /usr/** 등 모든
	 * 엔드포인트에 적용해 403(CORS) 방지
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		// 크리덴셜 사용 시 AllowedOrigins에 와일드카드("*") 금지
		cfg.setAllowedOrigins(List.of("https://aniwell.s3.ap-northeast-2.amazonaws.com",
				"http://aniwell.s3-website.ap-northeast-2.amazonaws.com", "http://localhost:3001",
				"http://localhost:8080"));
		// 필요 시 CloudFront/운영 도메인 추가
		// cfg.setAllowedOriginPatterns(List.of("https://*.cloudfront.net",
		// "https://www.aniwell.kr"));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setExposedHeaders(List.of("Location"));
		cfg.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// :흰색_확인_표시: 전역 적용: /api/** 뿐 아니라 /usr/** 까지
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(8);
	}
}