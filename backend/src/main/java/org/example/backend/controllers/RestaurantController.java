package org.example.backend.controllers;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.services.RestaurantService;
import org.example.backend.services.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final SessionService sessionService;

    public RestaurantController(RestaurantService restaurantService, SessionService sessionService) {
        this.restaurantService = restaurantService;
        this.sessionService = sessionService;
    }

    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants(@RequestHeader("Authorization") String authorization) {
        User user = sessionService.getUserBySessionToken(extractSessionToken(authorization));
        return this.restaurantService.findFavoriteRestaurantsForUser(user);
    }

    @PostMapping("/restaurants")
    public Restaurant create(@RequestBody RestaurantDTO restaurantDTO, @RequestHeader("Authorization") String authorization) {
        User user = sessionService.getUserBySessionToken(extractSessionToken(authorization));
        return this.restaurantService.saveRestaurant(restaurantDTO, user);
    }

    @GetMapping("/restaurants/{placeId}")
    public Restaurant getRestaurant(@PathVariable String placeId) {
        return this.restaurantService.findRestaurantByPlaceId(placeId);
    }

    @DeleteMapping("/restaurants/{placeId}")
    public ResponseEntity<Restaurant> deleteRestaurant(@PathVariable String placeId, @RequestHeader("Authorization") String authorization) {
        User user = sessionService.getUserBySessionToken(extractSessionToken(authorization));
        return this.restaurantService.removeFavoriteRestaurant(placeId, user);
    }

    private String extractSessionToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidSessionTokenException("Missing or malformed Authorization header");
        }
        return authorization.substring("Bearer ".length());
    }
}
