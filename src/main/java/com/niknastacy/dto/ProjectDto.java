package com.niknastacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDto {

    @NotBlank(message = "Название проекта обязательно")
    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String name;
    private String description;

    @Pattern(
            regexp = "^$|^(https?:\\/\\/)?(www\\.)?github\\.com\\/[A-Za-z0-9_.-]+.*$",
            message = "Ссылка должна вести на github.com"
    )
    private String githubUrl;

    private String status = "open";
}
