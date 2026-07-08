package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.services.SessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionTokenAuthenticationFilterTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private SessionTokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SessionTokenAuthenticationFilter(sessionService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenBearerTokenIsValid() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("janedoe");

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(sessionService.getUserBySessionToken("valid-token")).thenReturn(user);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(user, Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldLeaveContextEmpty_whenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(sessionService.getUserBySessionToken("bad-token"))
                .thenThrow(new InvalidSessionTokenException("Session token is invalid or already expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldLeaveContextEmpty_whenAuthorizationHeaderIsMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldLeaveContextEmpty_whenAuthorizationHeaderIsMalformed() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("NotBearer sometoken");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
