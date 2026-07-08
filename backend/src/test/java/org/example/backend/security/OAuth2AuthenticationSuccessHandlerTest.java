package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.entities.User;
import org.example.backend.repos.UserRepository;
import org.example.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(userRepository, userService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:5173");
    }

    private OAuth2User oAuth2UserWithEmail(String email) {
        Map<String, Object> attributes = Map.of("id", "12345", "email", email, "login", "janedoe");
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
    }

    @Test
    void onAuthenticationSuccess_redirectsWithSessionToken_whenUserExists() throws Exception {
        User user = new User();
        user.setEmail("jane@example.com");
        Authentication authentication = new TestingAuthenticationToken(oAuth2UserWithEmail("jane@example.com"), null);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(userService.issueSessionToken(user)).thenReturn("session-token");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:5173?token=session-token");
    }

    @Test
    void onAuthenticationSuccess_throws_whenPrincipalIsNotOAuth2User() {
        Authentication authentication = new TestingAuthenticationToken("not-an-oauth2-user", null);

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected OAuth2User principal");

        verifyNoInteractions(userRepository, userService, response);
    }

    @Test
    void onAuthenticationSuccess_throws_whenUserNotFoundByEmail() {
        Authentication authentication = new TestingAuthenticationToken(oAuth2UserWithEmail("missing@example.com"), null);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OAuth2 user not found after login");

        verifyNoInteractions(userService, response);
    }
}
