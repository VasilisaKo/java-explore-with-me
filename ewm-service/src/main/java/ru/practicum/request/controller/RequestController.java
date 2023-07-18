package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class RequestController {
    private final RequestService requestService;

    @PostMapping(value = "/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto postRequest(@PathVariable Long userId, @RequestParam @NotNull long eventId) {
        return requestService.saveRequestPriv(userId, eventId);
    }

    @GetMapping(value = "/{userId}/requests")
    public List<ParticipationRequestDto> getRequest(@PathVariable Long userId) {
        return requestService.getRequestPriv(userId);
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto patchRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.patchRequestPriv(userId, requestId);
    }
}