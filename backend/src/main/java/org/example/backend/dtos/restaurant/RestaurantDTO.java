package org.example.backend.dtos.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record RestaurantDTO(
    @NotBlank(message = "PlaceId can not be blank") String placeId
) {
}
