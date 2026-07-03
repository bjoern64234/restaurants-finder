package org.example.backend.dtos.auth;

import lombok.Builder;
import lombok.With;

@With
@Builder
public record AuthResponse(String sessionToken) {
}
