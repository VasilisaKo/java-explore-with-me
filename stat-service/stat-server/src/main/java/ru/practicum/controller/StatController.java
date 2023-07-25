package ru.practicum.controller;

import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dtoMapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import model.EndpointHitDto;
import ru.practicum.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService statsService;

    @PostMapping(value = "/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveEndpointHit(@RequestBody EndpointHitDto dto) {
        EndpointHit endpointHit = EndpointHitMapper.toEntity(dto);
        endpointHit = statsService.saveEndpointHit(endpointHit);
        return EndpointHitMapper.toDto(endpointHit);
    }

    @GetMapping(value = "/stats")
    public ResponseEntity getViewStats(@RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        if (start.isAfter(end)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(statsService.getViewStats(start, end, uris, unique));
    }
}