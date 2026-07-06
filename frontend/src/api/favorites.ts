import {api} from "./axios";
import type {AxiosResponse} from "axios";
import type {FavoriteRestaurant, SaveFavoriteRestaurantParams} from "../types/restaurant.type";

export const getFavoriteRestaurants = (): Promise<AxiosResponse<FavoriteRestaurant[]>> => {
  return api.get("/restaurants");
};

export const saveFavoriteRestaurant = (
  params: SaveFavoriteRestaurantParams
): Promise<AxiosResponse<FavoriteRestaurant>> => {
  return api.post("/restaurants", params);
};

export const deleteFavoriteRestaurant = (id: number): Promise<AxiosResponse<void>> => {
  return api.delete(`/restaurants/${id}`);
};
