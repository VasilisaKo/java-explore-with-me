package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventPrivateService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class PrivateEventController {
    private final EventPrivateService eventPrivateService;

    @PostMapping(value = "/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto postEvent(@RequestBody @Valid NewEventDto dto, @PathVariable Long userId) {
        return eventPrivateService.saveEvent(dto, userId);
    }

    @GetMapping(value = "/{userId}/events")
    public List<EventShortDto> getEventsFromUser(@PathVariable Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") int from,
                                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        return eventPrivateService.getEventsFromUser(userId, from, size);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getEventById(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    public EventFullDto patchEvent(@RequestBody @Valid UpdateEventUserRequest dto, @PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.patchEvent(dto, userId, eventId);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getEventRequests(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult patchEventRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                             @RequestBody(required = false) EventRequestStatusUpdateRequest dto) {
        if (dto == null) {
            throw new ConflictException("Нет изменяемых данных");
        }
        return eventPrivateService.patchEventRequests(dto, userId, eventId);
    }
}
