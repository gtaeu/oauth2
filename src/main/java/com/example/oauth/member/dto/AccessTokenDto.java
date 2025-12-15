package com.example.oauth.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 무시한다
public class AccessTokenDto {
    private String access_token;

    // 사실 aT만 받아도 되긴하다
    private String expires_in;
    private String scope;
    private String id_token;

}
