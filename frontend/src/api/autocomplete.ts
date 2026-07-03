import {api} from "./axios";
import type {AxiosResponse} from "axios";
import type {AutocompleteResult} from "../types/autocomplete.type";

export const autocompleteLocations = (
    query: string,
    signal?: AbortSignal
): Promise<AxiosResponse<AutocompleteResult>> => {
    return api.get("/autocomplete", {
        params: {query},
        signal,
    });
};
