package org.example.backend.dtos.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record RestaurantDTO(
    @NotBlank(message = "Name can not be blank") String name,
    String formatted,
    String website,
    String openingHours,
    String cuisine,
    @NotNull(message = "Lat can not be null") Double lat,
    @NotNull(message = "Lng can not be null")  Double lng,
    @NotBlank(message = "PlaceId can not be blank") String placeId
) {
}
