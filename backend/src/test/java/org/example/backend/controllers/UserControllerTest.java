package org.example.backend.controllers;

import org.example.backend.dtos.auth.LoginRequest;
import org.example.backend.dtos.auth.RegisterRequest;
import org.example.backend.entities.User;
import org.example.backend.exceptions.DuplicateUserException;
import org.example.backend.exceptions.InvalidCredentialsException;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.repos.UserRepository;
import org.example.backend.services.SessionService;
import org.example.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SessionService sessionService;

    private User user;
    @Autowired
    private ObjectMapper objectMapper;

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
        when(sessionService.extractSessionToken("Bearer valid-token")).thenReturn("valid-token");
        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(userService).logout(eq("valid-token"));
    }

    @Test
    void logout_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        when(sessionService.extractSessionToken("Bearer bad-token")).thenReturn("bad-token");
        doThrow(new InvalidSessionTokenException("Session token is invalid or already expired"))
                .when(userService).logout("bad-token");

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturnUnauthorized_whenAuthorizationHeaderIsMalformed() throws Exception {
        when(sessionService.extractSessionToken("NotBearer sometoken"))
                .thenThrow(new InvalidSessionTokenException("Missing or malformed Authorization header"));
        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "NotBearer sometoken"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void logout_shouldReturnBadRequest_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturnListOfUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("janedoe"));
    }

    @Test
    void getById_shouldReturnUser_whenFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"));
    }

    @Test
    void getById_shouldReturnEmptyBody_whenNotFound() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/99"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void update_shouldReturnUpdatedUser_whenFound() throws Exception {
        User updatePayload = new User();
        updatePayload.setName("Jane Updated");
        updatePayload.setEmail("jane.updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"))
                .andExpect(jsonPath("$.email").value("jane.updated@example.com"));
    }

    @Test
    void update_shouldReturnEmptyBody_whenNotFound() throws Exception {
        User updatePayload = new User();
        updatePayload.setName("Jane Updated");
        updatePayload.setEmail("jane.updated@example.com");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_shouldReturnOk_andDeleteUserById() throws Exception {
        mockMvc.perform(delete("/api/1"))
                .andExpect(status().isOk());

        verify(userRepository).deleteById(1L);
    }


}
