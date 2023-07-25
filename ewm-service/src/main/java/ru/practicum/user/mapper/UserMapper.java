package ru.practicum.user.mapper;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toEntity(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserShortDto toDtoShort(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}