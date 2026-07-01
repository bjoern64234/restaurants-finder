package org.example.backend.controllers;

import org.example.backend.dtos.autocomplete.AutocompleteResult;
import org.example.backend.dtos.search.SearchPlaceResponse;
import org.example.backend.services.GeoapifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeoapifyController.class)
class GeoapifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeoapifyService geoapifyService;

    private SearchPlaceResponse searchPlaceResponse;
    private AutocompleteResult autocompleteResult;

    @BeforeEach
    void setUp() {
        searchPlaceResponse = SearchPlaceResponse.builder().build();
        autocompleteResult = AutocompleteResult.builder().build();
    }

    @Test
    void search_shouldReturnOkWithValidParams() throws Exception {
        when(geoapifyService.searchLocations("52.5200", "13.4050", "5000", 10))
                .thenReturn(searchPlaceResponse);

        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "5000")
                        .param("limit", "10"))
                .andExpect(status().isOk());

        verify(geoapifyService).searchLocations("52.5200", "13.4050", "5000", 10);
    }

    @Test
    void search_shouldReturnBadRequestWhenLatIsBlank() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "")
                        .param("lng", "13.4050")
                        .param("radius", "5000")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenLngIsBlank() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "")
                        .param("radius", "5000")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenRadiusIsBlank() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenLimitIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "5000")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenLimitIsNegative() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "5000")
                        .param("limit", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenLimitIsMissing() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200")
                        .param("lng", "13.4050")
                        .param("radius", "5000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnBadRequestWhenRequiredParamsAreMissing() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("lat", "52.5200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autocomplete_shouldReturnOkWithValidQuery() throws Exception {
        when(geoapifyService.autocompleteLocations("Berlin"))
                .thenReturn(autocompleteResult);

        mockMvc.perform(get("/api/autocomplete")
                        .param("query", "Berlin"))
                .andExpect(status().isOk());

        verify(geoapifyService).autocompleteLocations("Berlin");
    }

    @Test
    void autocomplete_shouldReturnBadRequestWhenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/api/autocomplete")
                        .param("query", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autocomplete_shouldReturnBadRequestWhenQueryIsMissing() throws Exception {
        mockMvc.perform(get("/api/autocomplete"))
                .andExpect(status().isBadRequest());
    }
}