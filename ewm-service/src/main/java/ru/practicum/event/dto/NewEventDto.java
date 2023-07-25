package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.event.model.Location;

import javax.persistence.Column;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@SuperBuilder
public class NewEventDto {
    Long id;

    @NotBlank
    @NotNull
    @Size(max = 2000, min = 20)
    @Column(length = 2048)
    String annotation;

    @NotNull
    Long category;

    @NotNull
    @Size(max = 7000, min = 20)
    @Column(length = 16384)
    String description;

    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    Location location;

    Boolean paid;

    Long participantLimit;

    Boolean requestModeration;

    @NotNull
    @Size(max = 120, min = 3)
    String title;
}
