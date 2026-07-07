package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.exceptions.DuplicateUserException;
import org.example.backend.exceptions.InvalidCredentialsException;
import org.example.backend.repos.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final long SESSION_TOKEN_DURATION_HOURS = 24;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException(
                    "Username '" + request.username() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(
                    "Email '" + request.email() + "' is already registered");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String sessionToken = UUID.randomUUID().toString();
        user.setSessionToken(sessionToken);
        user.setSessionTokenExpiresAt(LocalDateTime.now().plusHours(SESSION_TOKEN_DURATION_HOURS));
        userRepository.save(user);

        return sessionToken;
    }

    public void logout(String sessionToken) {
        User user = sessionService.getUserBySessionToken(sessionToken);
        user.setSessionToken(null);
        user.setSessionTokenExpiresAt(null);
        userRepository.save(user);
    }
}
