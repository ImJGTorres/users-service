package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequestDto {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "\\d+", message = "must contain only digits")
    private String identityDocument;

    @NotBlank
    @Size(max = 13)
    @Pattern(regexp = "\\+?\\d+", message = "must contain only digits and an optional leading +")
    private String phone;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 72)
    private String password;
}
