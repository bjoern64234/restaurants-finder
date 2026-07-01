package org.example.backend.dtos.autocomplete;

import lombok.Builder;
import lombok.With;

import java.util.List;

@With
@Builder
public record AutocompleteResult(List<ResultsDTO> results) {
}
