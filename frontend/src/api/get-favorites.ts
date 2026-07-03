import {api} from "./axios";
import type {AxiosResponse} from "axios";
import type {FavoriteRestaurant} from "../types/restaurant.type";

export const getFavoriteRestaurants = (): Promise<AxiosResponse<FavoriteRestaurant[]>> => {
  return api.get("/restaurants");
};
