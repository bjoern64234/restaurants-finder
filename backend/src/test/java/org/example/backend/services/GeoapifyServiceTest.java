package org.example.backend.services;

import org.example.backend.dtos.autocomplete.AutocompleteResult;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeoapifyServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com/v2";
    private static final String AUTOCOMPLETE_BASE_URL = "https://api.geoapify.com/v1/geocode";

    private GeoapifyService geoapifyService;

    private MockRestServiceServer geoapifyMockServer;
    private MockRestServiceServer autocompleteMockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder geoapifyBuilder = RestClient.builder().baseUrl(GEOAPIFY_BASE_URL);
        geoapifyMockServer = MockRestServiceServer.bindTo(geoapifyBuilder).build();
        RestClient geoapifyRestClient = geoapifyBuilder.build();

        RestClient.Builder autocompleteBuilder = RestClient.builder().baseUrl(AUTOCOMPLETE_BASE_URL);
        autocompleteMockServer = MockRestServiceServer.bindTo(autocompleteBuilder).build();
        RestClient autocompleteRestClient = autocompleteBuilder.build();

        geoapifyService = new GeoapifyService(geoapifyRestClient, autocompleteRestClient);
        ReflectionTestUtils.setField(geoapifyService, "apiKey", TEST_API_KEY);
    }

    @Test
    void searchLocations_shouldSendCorrectRequestAndParseResponse() {
        // Arrange
        String lat = "52.5200";
        String lng = "13.4050";
        String radius = "5000";
        Integer limit = 10;

        String mockJsonResponse = """
                {
                  "features": []
                }
                """;

        geoapifyMockServer.expect(requestTo(GEOAPIFY_BASE_URL + "/places" +
                        "?categories=catering.restaurant" +
                        "&filter=circle:" + lng + "," + lat + "," + radius +
                        "&bias=proximity:" + lng + "," + lat +
                        "&limit=" + limit +
                        "&apiKey=" + TEST_API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        // Act
        SearchPlaceResponse response = geoapifyService.searchLocations(lat, lng, radius, limit);

        // Assert
        assertNotNull(response);
        geoapifyMockServer.verify();
    }

    @Test
    void autocompleteLocations_shouldSendCorrectRequestAndParseResponse() {
        // Arrange
        String query = "Berlin";

        String mockJsonResponse = """
                {
                  "results": []
                }
                """;

        autocompleteMockServer.expect(requestTo(AUTOCOMPLETE_BASE_URL + "/autocomplete" +
                        "?text=" + query +
                        "&limit=3" +
                        "&format=json" +
                        "&apiKey=" + TEST_API_KEY))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        // Act
        AutocompleteResult result = geoapifyService.autocompleteLocations(query);

        // Assert
        assertNotNull(result);
        autocompleteMockServer.verify();
    }
}