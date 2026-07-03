package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.services.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<Restaurant> getRestaurants() {
        return this.restaurantService.findAllRestaurants();
    }

    @PostMapping
    public Restaurant create(@RequestBody RestaurantDTO restaurantDTO) {
        return this.restaurantService.saveRestaurant(restaurantDTO);
    }

    @GetMapping("/{id}")
    public Restaurant getRestaurant(@PathVariable Long id) {
        return this.restaurantService.findRestaurantById(id);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        return this.restaurantService.deleteRestaurant(id);
    }
}
