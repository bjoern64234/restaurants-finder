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
  if (!/^[A-Za-z0-9_-]+$/.test(id)) {
    throw new Error("Invalid place id");
  }
  return protectedApi.delete(`/restaurants/${id}`);
};
