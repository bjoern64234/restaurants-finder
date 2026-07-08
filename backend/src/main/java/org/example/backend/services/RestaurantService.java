package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.repos.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public List<Restaurant> findAllRestaurants() {
        return this.restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Restaurant> findFavoriteRestaurantsForUser(User user) {
        return new ArrayList<>(managedUser(user).getFavorite_restaurants());
    }

    @Transactional
    public Restaurant saveRestaurant(RestaurantDTO restaurantDTO, User user) {
        // Check if restaurant already exists
        Restaurant restaurant = restaurantRepository.findRestaurantByPlaceId(restaurantDTO.placeId())
                .orElseGet(() -> {
                    Restaurant r = new Restaurant();
                    r.setPlaceId(restaurantDTO.placeId());
                    return restaurantRepository.save(r);
                });

        User managedUser = managedUser(user);

        // Update both sides of the relationship
        managedUser.getFavorite_restaurants().add(restaurant);
        restaurant.getUsers().add(managedUser);

        return restaurant;
    }

    public Restaurant findRestaurantByPlaceId(String placeId) {
        return this.restaurantRepository.findRestaurantByPlaceId(placeId).orElseThrow(() -> new RestaurantNotFoundException(placeId));
    }

    @Transactional
    public ResponseEntity<Restaurant> removeFavoriteRestaurant(String placeId, User user) {
        Restaurant restaurant = this.restaurantRepository.findRestaurantByPlaceId(placeId)
                .orElseThrow(() -> new RestaurantNotFoundException(placeId));

        managedUser(user).getFavorite_restaurants().remove(restaurant);

        return ResponseEntity.ok().build();
    }

    private User managedUser(User user) {
        return this.userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists: " + user.getId()));
    }
}