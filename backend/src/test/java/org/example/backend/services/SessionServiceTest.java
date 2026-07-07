package org.example.backend.services;

import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private UserRepository userRepository;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(userRepository);
    }

    @Test
    void getUserBySessionToken_shouldReturnUser_whenTokenIsValidAndNotExpired() {
        User user = new User();
        user.setSessionToken("valid-token");
        user.setSessionTokenExpiresAt(LocalDateTime.now().plusHours(1));

        when(userRepository.findBySessionToken("valid-token")).thenReturn(Optional.of(user));

        User result = sessionService.getUserBySessionToken("valid-token");

        assertEquals(user, result);
    }

    @Test
    void getUserBySessionToken_shouldThrow_whenTokenDoesNotExist() {
        when(userRepository.findBySessionToken("unknown-token")).thenReturn(Optional.empty());

        assertThrows(InvalidSessionTokenException.class,
                () -> sessionService.getUserBySessionToken("unknown-token"));
    }

    @Test
    void getUserBySessionToken_shouldThrow_whenTokenIsExpired() {
        User user = new User();
        user.setSessionToken("expired-token");
        user.setSessionTokenExpiresAt(LocalDateTime.now().minusHours(1));

        when(userRepository.findBySessionToken("expired-token")).thenReturn(Optional.of(user));

        assertThrows(InvalidSessionTokenException.class,
                () -> sessionService.getUserBySessionToken("expired-token"));
    }

    @Test
    void extractSessionToken_shouldReturnToken_whenHeaderIsValid() {
        assertEquals("abc", sessionService.extractSessionToken("Bearer abc"));
    }

    @Test
    void extractSessionToken_shouldThrow_whenHeaderIsNullOrMalformed() {
        assertThrows(InvalidSessionTokenException.class, () -> sessionService.extractSessionToken(null));
        assertThrows(InvalidSessionTokenException.class, () -> sessionService.extractSessionToken("NotBearer x"));
    }
}
