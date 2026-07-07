package org.example.backend.services;

import org.example.backend.dtos.search.FeaturesDTO;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoritesService {
    @Value("${GEOAPIFY_API_KEY}")
    private String apiKey;
    private final SessionService sessionService;
    private final RestClient geoapifyRestClient;

    public FavoritesService(RestClient geoapifyRestClient, SessionService sessionService) {
        this.sessionService = sessionService;
        this.geoapifyRestClient = geoapifyRestClient;
    }


    public SearchPlaceResponse findFavorites(String token) {
        User actualUser = sessionService.getUserBySessionToken(token);

        List<FeaturesDTO> allFeatures = new ArrayList<>();
        for (Restaurant fav : actualUser.favorite_restaurants) {
            try {
                SearchPlaceResponse response = geoapifyRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("place-details")
                                .queryParam("id", fav.placeId)
                                .queryParam("apiKey", apiKey)
                                .build())
                        .retrieve()
                        .body(SearchPlaceResponse.class);

                if (response != null) {
                    allFeatures.addAll(response.features());
                }
            } catch (HttpClientErrorException.TooManyRequests |
                     HttpClientErrorException.NotFound _) {
                // Rate limit (429) oder Favorit nicht mehr vorhanden (404):
                // diesen Favoriten überspringen, damit die restliche Liste durchkommt.
            }
        }
        return SearchPlaceResponse.builder().features(allFeatures).build();
    }
}
