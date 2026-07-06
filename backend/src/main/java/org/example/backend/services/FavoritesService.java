package org.example.backend.services;

import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.entities.User;
import org.example.backend.exceptions.InvalidSessionTokenException;
import org.example.backend.repos.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FavoritesService {

    private final UserRepository userRepo;
    private final RestClient geoapifyRestClient;

    public FavoritesService(RestClient geoapifyRestClient, UserRepository userRepo) {
        this.userRepo = userRepo;
        this.geoapifyRestClient = geoapifyRestClient;
    }


    public SearchPlaceResponse findFavorites(String token) {
        User actualUser = userRepo.findBySessionToken(token)
                .orElseThrow(() -> new InvalidSessionTokenException("Session token is invalid or already expired"));

        return actualUser.favorites();
    }
}
