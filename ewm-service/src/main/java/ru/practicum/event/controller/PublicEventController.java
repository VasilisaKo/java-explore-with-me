package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventPublicService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
@Slf4j
public class PublicEventController {
    private final EventPublicService eventPublicService;

    @GetMapping(value = "/{id}")
    public EventFullDto getEventPub(@PathVariable Long id, HttpServletRequest request) {
        return eventPublicService.getEvent(id, request.getRemoteAddr());
    }

    @GetMapping
    public List<EventShortDto> getListEventPub(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false) String rangeStart,
                                               @RequestParam(required = false) String rangeEnd,
                                               @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size,
                                               HttpServletRequest request) {
        return eventPublicService.getSearchEvent(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                from, size, request.getRemoteAddr());
    }
}