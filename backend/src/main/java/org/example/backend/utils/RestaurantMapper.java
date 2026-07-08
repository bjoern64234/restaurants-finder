package org.example.backend.utils;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.springframework.stereotype.Service;

@Service
public class RestaurantMapper {

    public Restaurant toEntity(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant();

        restaurant.setPlaceId(restaurantDTO.placeId());

        return restaurant;
    }
}