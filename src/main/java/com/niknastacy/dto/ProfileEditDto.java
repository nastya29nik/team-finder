package com.niknastacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileEditDto {

    @NotBlank(message = "Имя обязательно")
    @Size(max = 124, message = "Максимальная длина имени 124 символа")
    private String name;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 124, message = "Максимальная длина фамилии 124 символа")
    private String surname;

    @Size(max = 256, message = "Описание профиля не должно превышать 256 символов")
    private String about;

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "^(\\+7|8)\\d{10}$", message = "Номер телефона должен быть в формате +7XXXXXXXXXX или 8XXXXXXXXXX")
    private String phone;

    @Pattern(regexp = "^$|^(https?:\\/\\/)?(www\\.)?github\\.com\\/[A-Za-z0-9_.-]+.*$", message = "Ссылка должна вести на github.com")
    private String githubUrl;
}