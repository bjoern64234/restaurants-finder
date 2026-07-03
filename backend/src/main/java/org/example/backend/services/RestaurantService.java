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

    public Restaurant findRestaurantById(long id) {
        return this.restaurantRepository.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    public ResponseEntity<Void> deleteRestaurant(long id) {
        this.restaurantRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}