package com.cnom.orderflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Email
        String email,

        @NotNull
        @Size(min = 8, max = 20)
        String plainTextPassword
) {
}
