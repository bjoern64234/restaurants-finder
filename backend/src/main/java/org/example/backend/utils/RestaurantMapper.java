package org.example.backend.utils;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.springframework.stereotype.Service;

@Service
public class RestaurantMapper {

    public Restaurant toEntity(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant();

        restaurant.setName(restaurantDTO.name());
        restaurant.setFormatted(restaurantDTO.formatted());
        restaurant.setWebsite(restaurantDTO.website());
        restaurant.setOpeningHours(restaurantDTO.openingHours());
        restaurant.setPhone(restaurantDTO.phone());
        restaurant.setCuisine(restaurantDTO.cuisine());
        restaurant.setLat(restaurantDTO.lat());
        restaurant.setLng(restaurantDTO.lng());
        restaurant.setPlaceId(restaurantDTO.placeId());

        return restaurant;
    }
}