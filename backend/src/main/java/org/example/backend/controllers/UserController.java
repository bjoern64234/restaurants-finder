package org.example.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.auth.AuthResponse;
import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.repos.UserRepository;
import org.example.backend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserRepository repo;
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
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(extractSessionToken(authorization));
        return ResponseEntity.noContent().build();
    }

    private String extractSessionToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidSessionTokenException("Missing or malformed Authorization header");
        }
        return authorization.substring("Bearer ".length());
    }

    // FOR DEBUG
    // READ ALL
    @GetMapping
    public List<User> getAll() {
        return repo.findAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    // UPDATE
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User newUser) {
        return repo.findById(id).map(user -> {
            user.setName(newUser.getName());
            user.setEmail(newUser.getEmail());
            return repo.save(user);
        }).orElse(null);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
