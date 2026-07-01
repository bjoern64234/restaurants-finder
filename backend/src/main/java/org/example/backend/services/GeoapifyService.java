package org.example.backend.services;

import org.example.backend.dtos.search.SearchPlaceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeoapifyService {

    @Value("${GEOAPIFY_API_KEY}")
    private String apiKey;
    private final RestClient geoapifyRestClient;

    public GeoapifyService(RestClient geoapifyRestClient) {
        this.geoapifyRestClient = geoapifyRestClient;
    }

    public SearchPlaceResponse searchLocations(String lat, String lng, String radius, Integer limit) {
        return this.geoapifyRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/places")
                        .queryParam("categories", "catering.restaurant")
                        .queryParam("filter",
                                "circle:" + lng + "," + lat + "," + radius)
//                        .queryParam("bias", "proximity:" + lng + "," + lat)
                        .queryParam("limit", limit)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .body(SearchPlaceResponse.class);
    }
}