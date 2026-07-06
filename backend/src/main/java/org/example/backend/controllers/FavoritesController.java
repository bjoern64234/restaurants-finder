package org.example.backend.controllers;

import jakarta.validation.constraints.NotBlank;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.services.FavoritesService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
public class FavoritesController {
    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @GetMapping("/favorites")
    public SearchPlaceResponse searchForFavorites(@RequestParam @NotBlank(
            message = "token can not be blank") String token) {
        return this.favoritesService.findFavorites(token);
    }

}
