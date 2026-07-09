package org.example.backend.controllers;

import org.example.backend.configurations.SecurityConfig;
import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.exceptions.DuplicateUserException;
import org.example.backend.exceptions.InvalidCredentialsException;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.security.OAuth2AuthenticationSuccessHandler;
import org.example.backend.security.OAuthUserService;
import org.example.backend.services.SessionService;
import org.example.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private OAuthUserService oAuthUserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

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
    void register_shouldReturnCreated_whenRequestIsValid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .username("janedoe")
                .password("password123")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("janedoe"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenFieldsAreBlank() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("")
                .email("not-an-email")
                .username("")
                .password("short")
                .build();

        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void register_shouldReturnConflict_whenUserAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .username("janedoe")
                .password("password123")
                .build();

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateUserException("Username 'janedoe' is already taken"));

        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturnOkWithSessionToken_whenCredentialsAreValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("janedoe")
                .password("password123")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn("session-token");

        mockMvc.perform(post("/api/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").value("session-token"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("janedoe")
                .password("wrongpassword")
                .build();

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnBadRequest_whenFieldsAreBlank() throws Exception {
        LoginRequest request = LoginRequest.builder().username("").password("").build();

        mockMvc.perform(post("/api/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void logout_shouldReturnNoContent_whenTokenIsValid() throws Exception {
        when(sessionService.getUserBySessionToken("valid-token")).thenReturn(user);

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(userService).logout(user);
    }

    @Test
    void logout_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(sessionService.getUserBySessionToken("bad-token"))
                .thenThrow(new InvalidSessionTokenException("Session token is invalid or already expired"));

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void logout_shouldReturnUnauthorized_whenAuthorizationHeaderIsMalformed() throws Exception {
        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "NotBearer sometoken"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void logout_shouldReturnUnauthorized_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }
}
