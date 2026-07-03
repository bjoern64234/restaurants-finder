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

    private final UserRepository userRepository;

    public User getUserBySessionToken(String sessionToken) {
        User user = userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new InvalidSessionTokenException("Session token is invalid or already expired"));

        if (user.getSessionTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidSessionTokenException("Session token is invalid or already expired");
        }

        return user;
    }
}
