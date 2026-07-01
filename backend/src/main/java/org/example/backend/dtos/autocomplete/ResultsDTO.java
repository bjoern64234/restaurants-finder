package org.example.backend.dtos.autocomplete;

import lombok.Builder;
import lombok.With;

@With
@Builder
public record ResultsDTO(
        String country,
        String country_code,
        String city,
        String postcode,
        String district,
        String suburb,
        String street,
        String formatted,
        Double lon,
        Double lat,
        String place_id
) {
}
