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
						.requestMatchers("/api/public/**").permitAll()
						.requestMatchers(HttpMethod.GET,
								"/api/pet/report",
								"/api/pet/weight-timeline"
						).permitAll()
						.requestMatchers("/api/pet/**").authenticated()
						.requestMatchers("/api/member/**").authenticated()
						.anyRequest().permitAll()
				)
				.exceptionHandling(e -> e
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				);
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/usr/**"))
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation(sessionFixation -> sessionFixation.none())
				)
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/pet/report").permitAll()
						.requestMatchers(
								"/", "/usr/home/main",
								"/usr/member/login", "/usr/member/doLogin",
								"/usr/member/join", "/usr/member/doJoin",
								"/usr/member/doFindLoginId", "/usr/member/doFindLoginPw",
								"/usr/member/findLoginId", "/usr/member/findLoginPw",
								"/usr/member/naver/**", "/usr/member/kakao/**", "/usr/member/google/**",
								"/usr/member/social-login",
								"/usr/member/firebase-session-login",
								"/usr/member/getLoginIdDup",
								"/usr/member/getEmailDup",
								"/usr/member/getNicknameDup",
								"/usr/member/getCellphoneDup",
								"/css/**", "/js/**", "/img/**", "/img.socialLogin/**",
								"/resource/**",
								"/favicon.ico"
						).permitAll()
						.requestMatchers(HttpMethod.GET, "/usr/pet/daily/**").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/usr/pet/daily/**").permitAll()
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
						// ✅ remember-me 쿠키도 함께 제거
						.deleteCookies("JSESSIONID", "REMEMBER_ME")
						.permitAll()
				)
				.rememberMe(rememberMe -> rememberMe
						.rememberMeCookieName("REMEMBER_ME")          // 쿠키 이름 명시
						.tokenValiditySeconds(14 * 24 * 60 * 60)      // 14일 유지
						.alwaysRemember(true)                         // 체크박스 없어도 자동 발급
						.useSecureCookie(true)                        // HTTPS 환경에서만 전송
				);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of(
				"https://aniwell.s3.ap-northeast-2.amazonaws.com",
				"http://aniwell.s3-website.ap-northeast-2.amazonaws.com",
				"http://localhost:3001",
				"http://localhost:8080"
		));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setExposedHeaders(List.of("Location"));
		cfg.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(8);
	}
}
