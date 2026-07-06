package org.example.backend.services;

import org.example.backend.dtos.restaurant.RestaurantDTO;
import org.example.backend.entities.Restaurant;
import org.example.backend.exceptions.restaurant.RestaurantNotFoundException;
import org.example.backend.repos.RestaurantRepository;
import org.example.backend.utils.RestaurantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(restaurantRepository, restaurantMapper);
    }

    @Test
    void findAllRestaurants_shouldReturnListOfRestaurants() {
        // Given
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("Pizza Place");
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("Sushi Bar");
        List<Restaurant> expectedRestaurants = List.of(restaurant1, restaurant2);
        when(restaurantRepository.findAll()).thenReturn(expectedRestaurants);

        // When
        List<Restaurant> result = restaurantService.findAllRestaurants();

        // Than
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedRestaurants);
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void findAllRestaurants_shouldReturnEmptyList_whenNoRestaurantsExist() {
        // Given
        when(restaurantRepository.findAll()).thenReturn(List.of());

        // When
        List<Restaurant> result = restaurantService.findAllRestaurants();

        // Than
        assertThat(result).isEmpty();
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void saveRestaurant_shouldMapDtoToEntityAndSave() {
        // Given
        RestaurantDTO dto = new RestaurantDTO(
                "Pizza Place",
                "123 Main St, City",
                "https://pizzaplace.com",
                "Mo-Su 10:00-22:00",
                "123456789",
                "Italian",
                52.5200,
                13.4050,
                "place-123"
        );

        Restaurant mappedRestaurant = new Restaurant();
        mappedRestaurant.setName(dto.name());
        mappedRestaurant.setFormatted(dto.formatted());
        mappedRestaurant.setWebsite(dto.website());
        mappedRestaurant.setOpeningHours(dto.openingHours());
        mappedRestaurant.setPhone(dto.phone());
        mappedRestaurant.setCuisine(dto.cuisine());
        mappedRestaurant.setLat(dto.lat());
        mappedRestaurant.setLng(dto.lng());
        mappedRestaurant.setPlaceId(dto.placeId());

        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setName(dto.name());
        savedRestaurant.setPlaceId(dto.placeId());

        when(restaurantMapper.toEntity(dto)).thenReturn(mappedRestaurant);
        when(restaurantRepository.save(mappedRestaurant)).thenReturn(savedRestaurant);

        // When
        Restaurant result = restaurantService.saveRestaurant(dto);

        // Than
        assertThat(result).isEqualTo(savedRestaurant);
        assertThat(result.getName()).isEqualTo("Pizza Place");
        assertThat(result.getPlaceId()).isEqualTo("place-123");

        verify(restaurantMapper, times(1)).toEntity(dto);

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(mappedRestaurant);
    }

    @Test
    void findRestaurantById_shouldReturnRestaurant_whenFound() {
        // Given
        long id = 1L;
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Pizza Place");
        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        // When
        Restaurant result = restaurantService.findRestaurantById(id);

        // Than
        assertThat(result).isEqualTo(restaurant);
        verify(restaurantRepository, times(1)).findById(id);
    }

    @Test
    void findRestaurantById_shouldThrowException_whenNotFound() {
        // Given
        long id = 99L;
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        // When & Assert
        assertThatThrownBy(() -> restaurantService.findRestaurantById(id))
                .isInstanceOf(RestaurantNotFoundException.class);
        verify(restaurantRepository, times(1)).findById(id);
    }

    @Test
    void deleteRestaurant_shouldCallRepositoryAndReturnOk() {
        // Given
        long id = 1L;
        doNothing().when(restaurantRepository).deleteById(id);

        // When
        ResponseEntity<Void> response = restaurantService.deleteRestaurant(id);

        // Than
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(restaurantRepository, times(1)).deleteById(id);
    }
}