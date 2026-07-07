package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.services.RestaurantService;
import org.example.backend.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private SessionService sessionService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setPlaceId("place-123");
    }

    // --- GET /api/restaurants ---

    @Test
    void getRestaurants_returnsListOfRestaurants() throws Exception {
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(restaurant));

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].placeId").value("place-123"));

        verify(restaurantService, times(1)).findAllRestaurants();
    }

    @Test
    void getRestaurants_returnsEmptyList_whenNoneExist() throws Exception {
        when(restaurantService.findAllRestaurants()).thenReturn(List.of());

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET /api/restaurants/{placeId} ---

    @Test
    void getRestaurant_returnsRestaurant_whenFound() throws Exception {
        when(restaurantService.findRestaurantByPlaceId("place-123")).thenReturn(restaurant);

        mockMvc.perform(get("/api/restaurants/place-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value("place-123"));

        verify(restaurantService).findRestaurantByPlaceId("place-123");
    }

    @Test
    void getRestaurant_returnsNull_whenNotFound() throws Exception {
        when(restaurantService.findRestaurantByPlaceId("unknown")).thenReturn(null);

        mockMvc.perform(get("/api/restaurants/unknown"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    // --- POST /api/restaurants ---

    @Test
    void create_returnsCreatedRestaurant_whenValidTokenProvided() throws Exception {
        RestaurantDTO dto = new RestaurantDTO("place-id");
        User user = new User();

        when(sessionService.getUserBySessionToken("valid-token")).thenReturn(user);
        when(restaurantService.saveRestaurant(any(RestaurantDTO.class), eq(user))).thenReturn(restaurant);

        mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value("place-123"));

        verify(sessionService).getUserBySessionToken("valid-token");
        verify(restaurantService).saveRestaurant(any(RestaurantDTO.class), eq(user));
    }

    @Test
    void create_throwsException_whenAuthorizationHeaderMissing() throws Exception {
        RestaurantDTO dto = new RestaurantDTO("place-id");

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(restaurantService);
    }

    @Test
    void create_throwsException_whenAuthorizationHeaderMalformed() throws Exception {
        RestaurantDTO dto = new RestaurantDTO("place-id");

        mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "InvalidTokenFormat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)));

        verifyNoInteractions(sessionService);
        verifyNoInteractions(restaurantService);
    }

    // --- DELETE /api/restaurants/{placeId} ---

    @Test
    void deleteRestaurant_returnsOk_whenDeleted() throws Exception {
        when(restaurantService.deleteRestaurantByPlaceId("place-123"))
                .thenReturn(ResponseEntity.ok(restaurant));

        mockMvc.perform(delete("/api/restaurants/place-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value("place-123"));

        verify(restaurantService).deleteRestaurantByPlaceId("place-123");
    }

    @Test
    void deleteRestaurant_returnsNotFound_whenRestaurantDoesNotExist() throws Exception {
        when(restaurantService.deleteRestaurantByPlaceId("unknown"))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(delete("/api/restaurants/unknown"))
                .andExpect(status().isNotFound());

        verify(restaurantService).deleteRestaurantByPlaceId("unknown");
    }
}