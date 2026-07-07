package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String PREFIX = "Bearer ";
    private final UserRepository userRepository;

    public String extractSessionToken(String authorization) {
        if (authorization == null || !authorization.startsWith(PREFIX)) {
            throw new InvalidSessionTokenException("Missing or malformed Authorization header");
        }
        return authorization.substring(PREFIX.length());
    }

    public User getUserBySessionToken(String sessionToken) {
        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new InvalidSessionTokenException("Session token is invalid or already expired"));

        if (user.getSessionTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidSessionTokenException("Session token is invalid or already expired");
        }

        return user;
    }
}
