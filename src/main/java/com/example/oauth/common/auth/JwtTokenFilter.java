package com.example.oauth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 토큰 검증 필터
@Component
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    // 헤더에서 토큰 까고 인증객체 만들어준다
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
        String token = httpServletRequest.getHeader("Authorization");

        try {
            if(token != null) {
                if(!token.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String jwtToken = token.substring(7);
                // 토큰 검증 및 claims 추출 (근데 서명할때는 디코딩해서 서명하고 파싱할때는 인코딩된 시크릿키로 서명검증하네.,?)
                Claims claims = Jwts.parserBuilder()
                        // 내부적으로 디코딩해서 사용
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody();
                // Authentication 객체 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                // 관례상 앞에 ROLE_을 붙여줘야 어노테이션 사용시 에러안ㅁ
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, jwtToken, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // 이렇게하면 컨텍스트홀더에서 .컨텍스트.인증.유저디테일에서 위에서 설정한 3가지(이메일, 비밀번호, 권한)을 가져올 수 있다
            }
            // 다시 securityChain 위로 돌아가라
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("invalid token");
        }

    }
}
