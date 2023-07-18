package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventPublicService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    private final UserService userService;

    private final EventPublicService eventPublicService;

    @Transactional
    public ParticipationRequestDto saveRequestPriv(Long userId, Long eventId) {
        User initiator = userService.findById(userId);
        Event event = eventPublicService.findById(eventId);
        checkPostRequest(event, userId);
        List<Request> checkExistingRequest = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        if (!checkExistingRequest.isEmpty()) {
            throw new ConflictException("The request has already exist");
        }
        Request request;
        if (event.getRequestModeration()) {
            request = new Request(eventId, LocalDateTime.now(), event, initiator, Status.PENDING);
        } else {
            request = new Request(eventId, LocalDateTime.now(), event, initiator, Status.CONFIRMED);
        }
        if (event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        request = requestRepository.save(request);
        ParticipationRequestDto dto = RequestMapper.toDto(request);
        return RequestMapper.toDto(request);
    }

    private void checkPostRequest(Event event, Long userId) {
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getParticipantLimit() != 0) {
            Long countRequest = requestRepository.countByEventId(event.getId());
            if (event.getParticipantLimit() <= countRequest) {
                throw new ConflictException("Достигнут лимит запросов на участие");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestPriv(Long userId) {
        userService.findById(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto patchRequestPriv(Long userId, Long requesId) {
        userService.findById(userId);
        Request request = requestRepository.findById(requesId).orElseThrow(() -> new ObjectNotFoundException("Request with id=" + requesId + " was not found"));
        request.setStatus(Status.CANCELED);
        request = requestRepository.save(request);
        return RequestMapper.toDto(request);
    }

}