import {protectedApi} from "./protectedApi";
import type {AxiosResponse} from "axios";
import type {FavoriteRestaurant, SaveFavoriteRestaurantParams} from "../types/restaurant.type";

export const getFavoriteRestaurants = (): Promise<AxiosResponse<FavoriteRestaurant[]>> => {
  return protectedApi.get("/restaurants");
};

export const saveFavoriteRestaurant = (
  params: SaveFavoriteRestaurantParams
): Promise<AxiosResponse<FavoriteRestaurant>> => {
  return protectedApi.post("/restaurants", params);
};

export const deleteFavoriteRestaurant = (id: string): Promise<AxiosResponse<void>> => {
  return protectedApi.delete(`/restaurants/${id}`);
};
