package com.example.oauth.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Slf4j
public class AuthTestController {

    @GetMapping("/{id}")
    public ResponseEntity<Long> test(@AuthenticationPrincipal User user, @PathVariable Long id) {
        log.info("유저정보: " + user);
        return ResponseEntity.ok(id);
    }
}
