package ru.practicum.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validator.EmailValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class NewUserRequest {
    long id;

    @NotBlank
    @Size(min = 2, max = 250)
    String name;

    @Size(min = 6, max = 254)
    @EmailValidation
    String email;
}
