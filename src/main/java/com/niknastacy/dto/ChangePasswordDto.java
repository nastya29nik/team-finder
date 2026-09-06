package com.niknastacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {

    @NotBlank(message = "Введите текущий пароль")
    private String oldPassword;

    @NotBlank(message = "Введите новый пароль")
    @Size(min = 4, message = "Новый пароль должен содержать минимум 4 символа")
    private String newPassword1;

    @NotBlank(message = "Повторите новый пароль")
    private String newPassword2;
}