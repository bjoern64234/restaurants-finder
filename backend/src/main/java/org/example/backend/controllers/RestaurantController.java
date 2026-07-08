package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.services.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public List<Restaurant> getRestaurants(@AuthenticationPrincipal User user) {
        return this.restaurantService.findFavoriteRestaurantsForUser(user);
    }

    @PostMapping("/restaurants")
    public Restaurant create(@RequestBody RestaurantDTO restaurantDTO, @AuthenticationPrincipal User user) {
        return this.restaurantService.saveRestaurant(restaurantDTO, user);
    }

    @GetMapping("/restaurants/{placeId}")
    public Restaurant getRestaurant(@PathVariable String placeId) {
        return this.restaurantService.findRestaurantByPlaceId(placeId);
    }

    @DeleteMapping("/restaurants/{placeId}")
    public ResponseEntity<Restaurant> deleteRestaurant(@PathVariable String placeId, @AuthenticationPrincipal User user) {
        return this.restaurantService.removeFavoriteRestaurant(placeId, user);
    }
}
