package com.example.oauth.member.controller;

import com.example.oauth.common.auth.JwtTokenProvider;
import com.example.oauth.member.dto.*;
import com.example.oauth.member.entity.Member;
import com.example.oauth.member.entity.SocialType;
import com.example.oauth.member.service.GoogleService;
import com.example.oauth.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;


    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    // 폼로그인이든 소셜로그인이든 로그인 = 토큰발급 이다
    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto memberLoginDto) {
        // email, password 일치한지 검증
        Member member = memberService.login(memberLoginDto);

        // 일치할 경우 jwt accesstoken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("id", member.getId());
        logInfo.put("token", jwtToken);

        return new ResponseEntity<>(logInfo, HttpStatus.OK);
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        // access token 발급(구글에서 토큰 말고도 여러 정보를 같이 주기 때문에 dto로 받는다)
        AccessTokenDto accessTokenDto = googleService.getAccessToken();
        // 사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());
        // 회원가입 안되어있으면 회원가입
        Member originalMember = memberService.getMemberBySocialId(googleProfileDto.getSub());
        // 회원가입 되어있으면 access token 발급
        if (originalMember == null) {
            originalMember = memberService.createOauth(googleProfileDto.getSub(), googleProfileDto.getEmail(), SocialType.GOOGLE);
        }

        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());

        //위에 폼로그인 밑부분 복붙
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("id", originalMember.getId());
        logInfo.put("token", jwtToken);

        return new ResponseEntity<>(logInfo, HttpStatus.OK);
    }
}
