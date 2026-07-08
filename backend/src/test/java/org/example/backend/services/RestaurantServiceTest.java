package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;
    private User user;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setPlaceId("place-123");

        user = new User();
        user.setId(1L);
        user.setFavorite_restaurants(new HashSet<>());
    }

    // --- findAllRestaurants ---

    @Test
    void findAllRestaurants_returnsAllRestaurants() {
        List<Restaurant> restaurants = List.of(restaurant);
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        List<Restaurant> result = restaurantService.findAllRestaurants();

        assertThat(result).isEqualTo(restaurants);
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void findAllRestaurants_returnsEmptyList_whenNoneExist() {
        when(restaurantRepository.findAll()).thenReturn(List.of());

        List<Restaurant> result = restaurantService.findAllRestaurants();

        assertThat(result).isEmpty();
    }

    // --- findFavoriteRestaurantsForUser ---

    @Test
    void findFavoriteRestaurantsForUser_returnsUsersFavorites() {
        user.getFavorite_restaurants().add(restaurant);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Restaurant> result = restaurantService.findFavoriteRestaurantsForUser(user);

        assertThat(result).containsExactly(restaurant);
    }

    @Test
    void findFavoriteRestaurantsForUser_returnsEmptyList_whenUserHasNoFavorites() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        List<Restaurant> result = restaurantService.findFavoriteRestaurantsForUser(user);

        assertThat(result).isEmpty();
    }

    // --- saveRestaurant ---

    @Test
    void saveRestaurant_addsRestaurantToFavorites_andReturnsRestaurant_whenRestaurantAlreadyExists() {
        RestaurantDTO dto = RestaurantDTO.builder().placeId("place-123").build();
        when(restaurantRepository.findRestaurantByPlaceId("place-123"))
                .thenReturn(Optional.of(restaurant));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Restaurant result = restaurantService.saveRestaurant(dto, user);

        assertThat(result).isEqualTo(restaurant);
        assertThat(user.getFavorite_restaurants()).contains(restaurant);
        assertThat(restaurant.getUsers()).contains(user);
        verify(restaurantRepository, never()).save(any());
        verify(userRepository).save(user);
    }

    @Test
    void saveRestaurant_createsNewRestaurant_whenPlaceIdNotFound() {
        RestaurantDTO dto = RestaurantDTO.builder().placeId("new-place").build();
        when(restaurantRepository.findRestaurantByPlaceId("new-place"))
                .thenReturn(Optional.empty());
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Restaurant result = restaurantService.saveRestaurant(dto, user);

        assertThat(result.getPlaceId()).isEqualTo("new-place");
        assertThat(user.getFavorite_restaurants()).contains(result);
        verify(restaurantRepository).save(any(Restaurant.class));
        verify(userRepository).save(user);
    }

    // --- findRestaurantByPlaceId ---

    @Test
    void findRestaurantByPlaceId_returnsRestaurant_whenFound() {
        when(restaurantRepository.findRestaurantByPlaceId("place-123"))
                .thenReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.findRestaurantByPlaceId("place-123");

        assertThat(result).isEqualTo(restaurant);
    }

    @Test
    void findRestaurantByPlaceId_throwsException_whenNotFound() {
        when(restaurantRepository.findRestaurantByPlaceId("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findRestaurantByPlaceId("unknown"))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    // --- removeFavoriteRestaurant ---

    @Test
    void removeFavoriteRestaurant_removesRestaurantFromFavorites_andReturnsOk() {
        user.getFavorite_restaurants().add(restaurant);
        when(restaurantRepository.findRestaurantByPlaceId("place-123"))
                .thenReturn(Optional.of(restaurant));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<Restaurant> result = restaurantService.removeFavoriteRestaurant("place-123", user);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getFavorite_restaurants()).doesNotContain(restaurant);
    }

    @Test
    void removeFavoriteRestaurant_throwsException_whenRestaurantNotFound() {
        when(restaurantRepository.findRestaurantByPlaceId("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.removeFavoriteRestaurant("unknown", user))
                .isInstanceOf(RestaurantNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}