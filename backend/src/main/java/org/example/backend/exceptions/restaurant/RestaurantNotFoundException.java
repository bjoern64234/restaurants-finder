package org.example.backend.exceptions.restaurant;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(long id) {
        super("Restaurant with id " + id + " not found");
    }
}
