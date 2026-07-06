package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.utils.RestaurantMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantService(RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
    }

    public List<Restaurant> findAllRestaurants() {
        return this.restaurantRepository.findAll();
    }

    public Restaurant saveRestaurant(RestaurantDTO restaurantDTO) {
        return this.restaurantRepository.save(this.restaurantMapper.toEntity(restaurantDTO));
    }

    public Restaurant findRestaurantByPlaceId(String placeId) {
        return this.restaurantRepository.findRestaurantByPlaceId(placeId).orElseThrow(() -> new RestaurantNotFoundException(placeId));
    }

    public ResponseEntity<Restaurant> deleteRestaurantByPlaceId(String placeId) {
        this.restaurantRepository.deleteRestaurantByPlaceId(placeId);

        return ResponseEntity.ok().build();
    }
}