package com.dev.order_fulfillment.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank String username,
                              @Size(min = 8, message = "Password must be at least 8 characters")String password,
                              @NotBlank @Email String email) {
}
