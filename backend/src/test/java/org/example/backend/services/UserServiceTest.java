package org.example.backend.services;

import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.exceptions.DuplicateUserException;
import org.example.backend.exceptions.InvalidCredentialsException;
import org.example.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void register_shouldSaveAndReturnUser_whenUsernameAndEmailAreAvailable() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .username("janedoe")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(request);

        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("janedoe", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
    }

    @Test
    void register_shouldThrow_whenUsernameIsAlreadyTaken() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .username("janedoe")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailIsAlreadyRegistered() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .username("janedoe")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnSessionToken_whenCredentialsAreValid() {
        LoginRequest request = LoginRequest.builder().username("janedoe").password("password123").build();

        User user = new User();
        user.setUsername("janedoe");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("janedoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String sessionToken = userService.login(request);

        assertNotNull(sessionToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(sessionToken, savedUser.getSessionToken());
        assertNotNull(savedUser.getSessionTokenExpiresAt());
        assertTrue(savedUser.getSessionTokenExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void login_shouldThrow_whenUsernameDoesNotExist() {
        LoginRequest request = LoginRequest.builder().username("unknown").password("password123").build();

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldThrow_whenPasswordDoesNotMatch() {
        LoginRequest request = LoginRequest.builder().username("janedoe").password("wrongpassword").build();

        User user = new User();
        user.setUsername("janedoe");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("janedoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void logout_shouldClearSessionToken() {
        User user = new User();
        user.setSessionToken("valid-token");
        user.setSessionTokenExpiresAt(LocalDateTime.now().plusHours(1));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.logout(user);

        assertNull(user.getSessionToken());
        assertNull(user.getSessionTokenExpiresAt());
        verify(userRepository).save(user);
    }
}
