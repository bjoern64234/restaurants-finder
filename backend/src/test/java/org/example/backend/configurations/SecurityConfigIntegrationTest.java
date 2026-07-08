package org.example.backend.configurations;

import org.example.backend.dtos.autocomplete.AutocompleteResult;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.entities.User;
import org.example.backend.repos.UserRepository;
import org.example.backend.services.GeoapifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private GeoapifyService geoapifyService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setUsername("janedoe");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setSessionToken("valid-token");
        user.setSessionTokenExpiresAt(LocalDateTime.now().plusHours(1));
        user = userRepository.save(user);
    }

    @Test
    void search_shouldBeAccessible_withoutAuthentication() throws Exception {
        when(geoapifyService.searchLocations("52.5200", "13.4050", "5000", null))
                .thenReturn(SearchPlaceResponse.builder().build());

        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "5000"))
                .andExpect(status().isOk());
    }

    @Test
    void autocomplete_shouldBeAccessible_withoutAuthentication() throws Exception {
        when(geoapifyService.autocompleteLocations("Berlin"))
                .thenReturn(AutocompleteResult.builder().build());

        mockMvc.perform(get("/api/autocomplete").param("query", "Berlin"))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldBeAccessible_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"John Smith","email":"john@example.com","username":"johnsmith","password":"password123"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void login_shouldBeAccessible_withoutAuthentication_andReturnSessionToken() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"janedoe","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").isNotEmpty());
    }

    @Test
    void session_shouldReturnUnauthorized_whenNoTokenIsProvided() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void session_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/session").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void session_shouldReturnOk_whenTokenIsValid() throws Exception {
        mockMvc.perform(get("/api/session").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"));
    }

    @Test
    void logout_shouldReturnUnauthorized_whenNoTokenIsProvided() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldClearSessionToken_whenTokenIsValid() throws Exception {
        mockMvc.perform(post("/api/logout").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findBySessionToken("valid-token").isEmpty());
    }
}
