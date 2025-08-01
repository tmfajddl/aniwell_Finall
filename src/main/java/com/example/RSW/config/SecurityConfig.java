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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", // 루트 URL
                                "/usr/home/main",   // 메인 페이지 URL 허용
                                "/usr/member/login", "/usr/member/doLogin",
                                "/usr/member/join", "/usr/member/doJoin",
                                "/css/**", "/js/**", "/img/**", "/resource/**"
                        ).permitAll()
                        .requestMatchers("/usr/pet/**").authenticated()  // ✅ 로그인 후만 접근 가능
                        .anyRequest().authenticated() // 그 외는 인증 필요
                )
                .formLogin(login -> login
                        .loginPage("/usr/member/login")
                        .defaultSuccessUrl("/", false) // ✅ false로 하면 이전 URL 유지
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/usr/member/doLogout")   // 로그아웃 URL
                        .logoutSuccessUrl("/")              // 로그아웃 후 메인 페이지로 이동
                        .invalidateHttpSession(true)        // 세션 무효화
                        .deleteCookies("JSESSIONID")        // 쿠키 삭제
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
