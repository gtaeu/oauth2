package com.example.oauth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
// 필요한 import 추가...

// 1. @Component 제거 (SecurityConfig에서 주입 권장)
// 2. OncePerRequestFilter 상속
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String secretKey;

    // 생성자 주입 방식으로 변경 (테스트 용이성 확보)
    public JwtTokenFilter(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 버그 수정: Request에서 헤더 추출
        String token = request.getHeader("Authorization");

        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);

                // 토큰 파싱
                Claims claims = Jwts.parserBuilder()
                        // secretKey가 Base64 인코딩 된 문자열이라고 가정하고 바이트로 변환
                        .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody();

                // Authentication 객체 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                // null 체크를 해주면 더 안전함
                Object role = claims.get("role");
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, jwtToken, authorities);

                // SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 2. 토큰이 없거나 검증 성공 시 다음 필터로 진행
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 3. 예외 처리: 여기서 바로 응답을 보낼지, 아니면 예외를 Security로 넘길지 결정해야 함.
            // 보통은 인증 실패 시 SecurityContext가 비어있으므로,
            // 뒤쪽의 ExceptionTranslationFilter가 알아서 401 처리를 하도록 두는 것이 깔끔할 수 있습니다.
            // 하지만 명시적으로 에러 메시지를 보내고 싶다면 작성하신 코드를 유지해도 됩니다.

            // 로그는 에러 상황 파악을 위해 필요
            logger.error("JWT Token Error", e);

            // 토큰이 잘못된 경우 아예 진행을 멈추고 에러 리턴
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Invalid Token: " + e.getMessage() + "\"}");
        }
    }
}
