import {protectedApi} from "./protectedApi.ts";
import type {AxiosResponse} from "axios";
import type {RestaurantResponse} from "../types/restaurant.type";


export const findFavoriteRestaurants = (): Promise<AxiosResponse<RestaurantResponse>> => {
    return protectedApi.get("/favorites");
};