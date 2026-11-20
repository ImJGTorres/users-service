package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequestDto extends AccountRequestDto {

    @NotNull
    @Past
    private LocalDate birthDate;
}
