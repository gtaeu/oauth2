package com.example.oauth.common.config;

import com.example.oauth.common.auth.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    // Configuration과 Bean의 조합으로 이 메서드가 리턴하는 객체를 스프링 컨테이너에 싱글톤 객체로 등록해줌
    // 나는 MemberService에서 PasswordEncoder(리턴값)만 의존성 주입해주면 된다
    // PasswordEncoder의 실제 구현체가 DelegatingPasswordEncoder임 encode랑 matches 메서드 있음
    @Bean
    public PasswordEncoder makePassword() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // Basic 인증 비활성화(비밀번호 그냥 인코딩하는거)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션 활성 비활성화
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 특정 api에 대해서 인증처리(Authentication 객체 생성) 제외
                .authorizeHttpRequests(a -> a.requestMatchers(
                        "/api/member/create",
                        "/api/member/login")
                        .permitAll()
                        .anyRequest().authenticated())
                // UsernamePasswordAuthenticationFilter 이거는 스프링에서 기본적으로 제공하는 폼로그인시 사용하는 필터인데
                // 우리는 폼로그인안쓸거임(이거는 화면단까지 만드는 MVC패턴에서 사용)
                // 그래서 UsernamePasswordAuthenticationFilter 전에 jwtTokenFilter 추가해서 인증객체를 만들어 저 토큰 무력화시킴
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 배포되면 url 추가
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 http 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 포스트맨 헤더탭의 모든 키 사용가능
        configuration.setAllowCredentials(true); // 자격 증명 허용(쿠키나 인증헤더 포함한 다른 url의 접근 허용)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 api 경로에 대하여 설정
        return source;
    }
}
