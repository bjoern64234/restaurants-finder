export interface RestaurantResponse {
    features: RestaurantFeature[];
}

export interface RestaurantFeature {
    properties: RestaurantProperties;
}

export interface RestaurantProperties {
    name: string;
    postcode: string;
    street: string;
    housenumber: string;
    city: string;
    formatted: string;
    website: string | null;
    opening_hours: string | null;
    contact: RestaurantContact | null;
    catering: RestaurantCatering | null;
    lon: number;
    lat: number;
    distance: number;
    place_id: string;
}

export interface RestaurantContact {
    phone: string;
}

export interface RestaurantCatering {
    cuisine: string;
}

export interface SaveFavoriteRestaurantParams {
    name: string;
    formatted: string;
    website: string | null;
    openingHours: string | null;
    phone: string | null;
    cuisine: string | null;
    lat: number;
    lng: number;
    placeId: string;
}

export interface FavoriteRestaurant extends SaveFavoriteRestaurantParams {
    id: number;
    createdAt: string;
    updatedAt: string;
}