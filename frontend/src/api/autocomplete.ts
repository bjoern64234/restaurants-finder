import {publicApi} from "./publicApi.ts";
import type {AxiosResponse} from "axios";
import type {AutocompleteResult} from "../types/autocomplete.type";

export const autocompleteLocations = (
    query: string,
    signal?: AbortSignal
): Promise<AxiosResponse<AutocompleteResult>> => {
    return publicApi.get("/geoapify/autocomplete", {
        params: {query},
        signal,
    });
};
