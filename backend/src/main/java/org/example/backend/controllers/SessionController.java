package org.example.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.entities.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class SessionController {

    @GetMapping("/session")
    public User getCurrentUser(@AuthenticationPrincipal User user) {
        return user;
    }
}
