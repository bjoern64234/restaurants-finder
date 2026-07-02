export type AutocompleteSuggestion = {
    country: string;
    country_code: string;
    city: string;
    postcode: string;
    district: string;
    suburb: string;
    street: string;
    formatted: string;
    lon: number;
    lat: number;
    place_id: string;
};

export type AutocompleteResult = {
    results: AutocompleteSuggestion[];
};
