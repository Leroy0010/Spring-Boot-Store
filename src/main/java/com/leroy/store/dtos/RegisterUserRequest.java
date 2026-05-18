package com.leroy.store.dtos;

import com.leroy.store.validation.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name should be between 3 and 255 characters")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email required")
    @Lowercase(message = "Email must be in lowercase")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 25, message = "Password should be between 6 and 25 characters")
    private String password;
}
