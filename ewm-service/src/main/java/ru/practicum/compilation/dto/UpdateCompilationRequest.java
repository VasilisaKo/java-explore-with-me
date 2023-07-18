package ru.practicum.compilation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class UpdateCompilationRequest {
    List<Long> events;

    Boolean pinned;

    @Size(min = 1, max = 50)
    String title;
}
