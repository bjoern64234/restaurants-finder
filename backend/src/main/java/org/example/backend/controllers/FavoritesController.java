package org.example.backend.controllers;

import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.services.FavoritesService;
import org.example.backend.services.SessionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FavoritesController {
    private final FavoritesService favoritesService;
    private final SessionService sessionService;

    public FavoritesController(FavoritesService favoritesService, SessionService sessionService) {
        this.favoritesService = favoritesService;
        this.sessionService = sessionService;
    }

    @GetMapping("/favorites")
    public SearchPlaceResponse searchForFavorites(@RequestHeader("Authorization") String authorization) {
        return this.favoritesService.findFavorites(sessionService.extractSessionToken(authorization));
    }
}
