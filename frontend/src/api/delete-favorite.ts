import {api} from "./axios";
import type {AxiosResponse} from "axios";

export const deleteFavoriteRestaurant = (id: number): Promise<AxiosResponse<void>> => {
  return api.delete(`/restaurants/${id}`);
};
