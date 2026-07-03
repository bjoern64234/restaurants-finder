package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.services.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants() {
        return this.restaurantService.findAllRestaurants();
    }

    @PostMapping("/restaurants")
    public Restaurant create(@RequestBody RestaurantDTO restaurantDTO) {
        return this.restaurantService.saveRestaurant(restaurantDTO);
    }

    @GetMapping("/restaurants/{id}")
    public Restaurant getRestaurant(@PathVariable Long id) {
        return this.restaurantService.findRestaurantById(id);
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        return this.restaurantService.deleteRestaurant(id);
    }
}
