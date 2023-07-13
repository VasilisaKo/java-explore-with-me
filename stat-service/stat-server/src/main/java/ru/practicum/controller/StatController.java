package ru.practicum.controller;

import com.sun.istack.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dtoMapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.EndpointHitDto;
import ru.practicum.model.ViewStatsDto;
import ru.practicum.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService statsService;

    @PostMapping(value = "/hit")
    public EndpointHitDto saveEndpointHit(@RequestBody EndpointHitDto dto) {
        EndpointHit endpointHit = EndpointHitMapper.toEntity(dto);
        endpointHit = statsService.saveEndpointHit(endpointHit);
        return EndpointHitMapper.toDto(endpointHit);
    }

    @GetMapping(value = "/stats")
    public List<ViewStatsDto> getViewStats(@RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(defaultValue = "false") boolean unique) {
        return statsService.getViewStats(start, end, uris, unique);
    }
}