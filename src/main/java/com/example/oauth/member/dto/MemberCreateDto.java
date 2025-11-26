package com.example.oauth.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateDto {
    // 어노테이션으로 이메일 형식, 비밀번호 형식 지정가능
    private String email;
    private String password;
}
