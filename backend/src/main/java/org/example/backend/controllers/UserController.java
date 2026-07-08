package org.example.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.auth.AuthResponse;
import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String sessionToken = userService.login(request);
        return new AuthResponse(sessionToken);
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        userService.logout(user);
        return ResponseEntity.noContent().build();
    }
}
