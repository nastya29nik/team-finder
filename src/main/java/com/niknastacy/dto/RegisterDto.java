package com.niknastacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 124, message = "Имя не должно превышать 124 символа")
    private String name;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 124, message = "Фамилия не должна превышать 124 символа")
    private String surname;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введите корректный email")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, message = "Пароль должен быть не менее 4 символов")
    private String password;
}
