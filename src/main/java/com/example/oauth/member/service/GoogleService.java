package com.example.oauth.member.service;

import com.example.oauth.member.dto.AccessTokenDto;
import com.example.oauth.member.dto.GoogleProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    public AccessTokenDto getAccessToken(String code) {
        // 인가코드, client
        // 서버와 서버 사이 통신하때 가장 많이 쓰는건 restTemplate인데 스프링에서 더이상 추천안함
        // 대신에 restClient 사용한다
        RestClient restClient = RestClient.create();

        // ?code=xxx&client_id=yyy& 이런식으로 할 수 있는데
        // MultiValueMap을 통해 자동으로 form-data 형식으로 조립가능
        // 알아서 파라미터 형식으로 조립해줌
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                // 우리가 보내고자 하는 데이터가 json이 아닌 폼데이터 형식으로 보내겠다는 설정
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                // retrieve: 응답 body값만 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        log.info("구글 accessToken JSON: " + response.getBody());

        // objectMapper 써서 파싱해서 보내도 된다
        return response.getBody();
    }

    public GoogleProfileDto getGoogleProfile(String token) {

        RestClient restClient = RestClient.create();

        ResponseEntity<GoogleProfileDto> response = restClient.post()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                // 우리가 보내고자 하는 데이터가 json이 아닌 폼데이터 형식이로 보내겠다는 설정
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        log.info("구글 profile JSON: " + response.getBody());

        return response.getBody();

    }
}
