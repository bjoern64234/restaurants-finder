package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.repos.UserRepository;
import org.example.backend.utils.RestaurantMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
        this.userRepository = userRepository;
    }

    public List<Restaurant> findAllRestaurants() {
        return this.restaurantRepository.findAll();
    }

    public List<Restaurant> findFavoriteRestaurantsForUser(User user) {
        return new ArrayList<>(user.getFavorite_restaurants());
    }

    public Restaurant saveRestaurant(RestaurantDTO restaurantDTO, User user) {
        Restaurant restaurant = this.restaurantRepository.findRestaurantByPlaceId(restaurantDTO.placeId())
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantDTO.placeId()));

        user.getFavorite_restaurants().add(restaurant);
        this.userRepository.save(user);

        return restaurant;
    }

    public Restaurant findRestaurantByPlaceId(String placeId) {
        return this.restaurantRepository.findRestaurantByPlaceId(placeId).orElseThrow(() -> new RestaurantNotFoundException(placeId));
    }

    public ResponseEntity<Restaurant> deleteRestaurantByPlaceId(String placeId) {
        this.restaurantRepository.deleteRestaurantByPlaceId(placeId);

        return ResponseEntity.ok().build();
    }
}