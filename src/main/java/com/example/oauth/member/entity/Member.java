package com.example.oauth.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // oauth로 생성돤 사용자는 비밀번호가 필요없어서 nullable = false 안해도된
    private String password;

    // enumerated string 안하면 순서대로 0, 1 이렇게 db에 들어감 가독성 위해서 이렇게
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    // 구글에서 우리서비스로 유일하게 주는 id(다른 사이트에서 같은 사용자가 oauth써도 다른 id 줌)
    private String socialId;
}
