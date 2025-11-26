package com.example.oauth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final int expiration;
    private Key signingKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        // 키를 암호화하는게 아니라 토큰 서명에 사용할 키의 규격(spec)을 명시 스트링을 바이트로 변환
        this.signingKey = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS256.getJcaName());
    }

    public String createToken(String email, String role) {
        // claims는 jwt의 페이로드를 의미하고 subject는 주 식별자로 userId나 이메일을 사용한다
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))
                .signWith(signingKey) // 시크릿키와 서명에 사용할 알고리즘 파라미터로 제시하면 헤더 페이로드 시크릿키 합쳐서 서명한다
                .compact();
        return token;
    }
}
