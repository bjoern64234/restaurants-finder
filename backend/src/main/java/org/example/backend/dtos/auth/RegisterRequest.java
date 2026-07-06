package org.example.backend.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record RegisterRequest(
        @NotBlank(message = "name can not be blank") String name,
        @NotBlank(message = "email can not be blank") @Email(message = "email must be valid") String email,
        @NotBlank(message = "username can not be blank") String username,
        @NotBlank(message = "password can not be blank") @Size(min = 8, message = "password must be at least 8 characters") String password
) {
}
