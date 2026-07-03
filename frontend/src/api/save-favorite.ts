import {api} from "./axios";
import type {AxiosResponse} from "axios";
import type {FavoriteRestaurant, SaveFavoriteRestaurantParams} from "../types/restaurant.type";

export const saveFavoriteRestaurant = (
  params: SaveFavoriteRestaurantParams
): Promise<AxiosResponse<FavoriteRestaurant>> => {
  return api.post("/restaurants", params);
};
