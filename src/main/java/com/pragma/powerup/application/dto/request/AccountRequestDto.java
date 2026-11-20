package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequestDto {

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

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 72)
    private String password;
}
