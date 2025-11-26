package com.example.oauth.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginDto {
    // 어노테이션으로 이메일 형식, 비밀번호 형식 지정가능
    // 실제 서비스는 회원가입할때 다른것도 받지만
    // 강의에서는 두개가 같아서 복붙
    private String email;
    private String password;
}
