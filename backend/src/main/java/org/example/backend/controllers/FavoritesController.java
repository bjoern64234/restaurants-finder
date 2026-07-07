package org.example.backend.controllers;

import jakarta.validation.constraints.NotBlank;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.exceptions.InvalidSessionTokenException;
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
    public SearchPlaceResponse searchForFavorites(@RequestHeader("Authorization") String authorization) {
        return this.favoritesService.findFavorites(extractSessionToken(authorization));
    }

    private String extractSessionToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidSessionTokenException("Missing or malformed Authorization header");
        }
        return authorization.substring("Bearer ".length());
    }

}
