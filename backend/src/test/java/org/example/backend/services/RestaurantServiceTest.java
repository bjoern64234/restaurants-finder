package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.repos.UserRepository;
import org.example.backend.utils.RestaurantMapper;
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
    private RestaurantMapper restaurantMapper;

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

        List<Restaurant> result = restaurantService.findFavoriteRestaurantsForUser(user);

        assertThat(result).containsExactly(restaurant);
    }

    @Test
    void findFavoriteRestaurantsForUser_returnsEmptyList_whenUserHasNoFavorites() {
        List<Restaurant> result = restaurantService.findFavoriteRestaurantsForUser(user);

        assertThat(result).isEmpty();
    }

    // --- saveRestaurant ---

    @Test
    void saveRestaurant_addsRestaurantToFavorites_andReturnsRestaurant() {
        RestaurantDTO dto = RestaurantDTO.builder().placeId("place-123").build();
        when(restaurantRepository.findRestaurantByPlaceId("place-123"))
                .thenReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.saveRestaurant(dto, user);

        assertThat(result).isEqualTo(restaurant);
        assertThat(user.getFavorite_restaurants()).contains(restaurant);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveRestaurant_throwsException_whenRestaurantNotFound() {
        RestaurantDTO dto = RestaurantDTO.builder().placeId("unknown-place").build();
        when(restaurantRepository.findRestaurantByPlaceId("unknown-place"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.saveRestaurant(dto, user))
                .isInstanceOf(RestaurantNotFoundException.class);

        verify(userRepository, never()).save(any());
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

    // --- deleteRestaurantByPlaceId ---

    @Test
    void deleteRestaurantByPlaceId_deletesRestaurant_andReturnsOk() {
        ResponseEntity<Restaurant> result = restaurantService.deleteRestaurantByPlaceId("place-123");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(restaurantRepository, times(1)).deleteRestaurantByPlaceId("place-123");
    }
}