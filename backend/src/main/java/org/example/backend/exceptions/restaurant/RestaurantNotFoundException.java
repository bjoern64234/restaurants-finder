package org.example.backend.exceptions.restaurant;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(String placeId) {
        super("Restaurant with placeId " + placeId + " not found");
    }
}
