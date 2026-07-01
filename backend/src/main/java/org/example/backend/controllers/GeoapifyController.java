package org.example.backend.controllers;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.services.GeoapifyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class GeoapifyController {

    private final GeoapifyService geoapifyService;

    public GeoapifyController(GeoapifyService geoapifyService) {
        this.geoapifyService = geoapifyService;
    }

    @GetMapping("/search")
    public SearchPlaceResponse search(@RequestParam @NotBlank(message = "lat can not be blank") String lat, @RequestParam @NotBlank(
            message = "lng can not be blank") String lng, @RequestParam @NotBlank(
            message = "radius can not be blank") String radius, @RequestParam @Positive(
            message = "limit must be a positive number") Integer limit) {
        return this.geoapifyService.searchLocations(lat, lng, radius, limit);
    }
}
