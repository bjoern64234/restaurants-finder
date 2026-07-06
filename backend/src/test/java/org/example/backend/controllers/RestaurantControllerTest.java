package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.exceptions.GlobalExceptionHandler;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestaurantController restaurantController = new RestaurantController(restaurantService);
        mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getRestaurants_shouldReturnListOfRestaurants() throws Exception {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Pizza Place");
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Sushi Bar");
        List<Restaurant> restaurants = List.of(restaurant1, restaurant2);

        when(restaurantService.findAllRestaurants()).thenReturn(restaurants);

        // When & Than
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Pizza Place"))
                .andExpect(jsonPath("$[1].name").value("Sushi Bar"));

        verify(restaurantService, times(1)).findAllRestaurants();
    }

    @Test
    void getRestaurants_shouldReturnEmptyList_whenNoRestaurantsExist() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(List.of());

        // When & Than
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(restaurantService, times(1)).findAllRestaurants();
    }

    @Test
    void create_shouldReturnCreatedRestaurant() throws Exception {
        // Given
        RestaurantDTO dto = new RestaurantDTO(
                "Pizza Place",
                "123 Main St, City",
                "https://pizzaplace.com",
                "Mo-Su 10:00-22:00",
                "123456789",
                "Italian",
                52.5200,
                13.4050,
                "place-123"
        );

        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setName(dto.name());
        savedRestaurant.setPlaceId(dto.placeId());

        when(restaurantService.saveRestaurant(any(RestaurantDTO.class))).thenReturn(savedRestaurant);

        // When & Than
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza Place"))
                .andExpect(jsonPath("$.placeId").value("place-123"));

        verify(restaurantService, times(1)).saveRestaurant(any(RestaurantDTO.class));
    }

    @Test
    void getRestaurant_shouldReturnRestaurant_whenFound() throws Exception {
        // Given
        long id = 1L;
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Pizza Place");

        when(restaurantService.findRestaurantById(id)).thenReturn(restaurant);

        // When & Than
        mockMvc.perform(get("/api/restaurants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza Place"));

        verify(restaurantService, times(1)).findRestaurantById(id);
    }

    @Test
    void getRestaurant_shouldReturnNotFound_whenRestaurantDoesNotExist() throws Exception {
        // Given
        long id = 99L;
        when(restaurantService.findRestaurantById(id)).thenThrow(new RestaurantNotFoundException(id));

        // When & Than
        mockMvc.perform(get("/api/restaurants/{id}", id))
                .andExpect(status().isNotFound());

        verify(restaurantService, times(1)).findRestaurantById(id);
    }

    @Test
    void deleteRestaurant_shouldReturnOk() throws Exception {
        // Given
        long id = 1L;
        when(restaurantService.deleteRestaurant(id)).thenReturn(ResponseEntity.ok().build());

        // When & Than
        mockMvc.perform(delete("/api/restaurants/{id}", id))
                .andExpect(status().isOk());

        verify(restaurantService, times(1)).deleteRestaurant(id);
    }
}