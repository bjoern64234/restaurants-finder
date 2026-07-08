package org.example.backend.controllers;

import org.example.backend.configurations.SecurityConfig;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.security.OAuth2AuthenticationSuccessHandler;
import org.example.backend.security.OAuthUserService;
import org.example.backend.security.RestAuthenticationEntryPoint;
import org.example.backend.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private OAuthUserService oAuthUserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setUsername("janedoe");
    }

    @Test
    void getCurrentUser_shouldReturnOkWithUser_whenTokenIsValid() throws Exception {
        when(sessionService.getUserBySessionToken("valid-token")).thenReturn(user);

        mockMvc.perform(get("/api/session")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(sessionService.getUserBySessionToken("bad-token"))
                .thenThrow(new InvalidSessionTokenException("Session token is invalid or already expired"));

        mockMvc.perform(get("/api/session")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorized_whenAuthorizationHeaderIsMalformed() throws Exception {
        mockMvc.perform(get("/api/session")
                        .header("Authorization", "NotBearer sometoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_shouldReturnUnauthorized_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized());
    }
}
