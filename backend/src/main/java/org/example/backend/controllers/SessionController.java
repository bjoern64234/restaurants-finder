package org.example.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.services.SessionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/session")
    public User getCurrentUser(@RequestHeader("Authorization") String authorization) {
        return sessionService.getUserBySessionToken(extractSessionToken(authorization));
    }

    private String extractSessionToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidSessionTokenException("Missing or malformed Authorization header");
        }
        return authorization.substring("Bearer ".length());
    }
}
