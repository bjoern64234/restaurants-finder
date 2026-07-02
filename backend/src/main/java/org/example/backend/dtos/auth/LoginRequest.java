package org.example.backend.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record LoginRequest(
        @NotBlank(message = "username can not be blank") String username,
        @NotBlank(message = "password can not be blank") String password
) {
}
