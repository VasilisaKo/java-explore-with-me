package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventAdminService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
@Slf4j
public class AdminEventController {
    private final EventAdminService eventAdminService;

    @GetMapping
    public List<EventFullDto> getEvent(@RequestParam(required = false) List<Long> users,
                                       @RequestParam(required = false) List<String> states,
                                       @RequestParam(required = false) List<Long> categories,
                                       @RequestParam(required = false) String rangeStart,
                                       @RequestParam(required = false) String rangeEnd,
                                       @RequestParam(name = "from", defaultValue = "0") int from,
                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventAdminService.getEvent(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping(value = "/{eventId}")
    public EventFullDto patchEvent(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest dto) {
        return eventAdminService.patchEvent(dto, eventId);
    }
}
