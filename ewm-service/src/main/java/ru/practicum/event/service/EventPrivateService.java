package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventPrivateService {
    private final EventRepository eventRepository;

    private final CategoryService categoryService;

    private final UserService userService;

    private final RequestRepository requestRepository;


    public EventFullDto saveEvent(NewEventDto dto, Long userId) {
        if (dto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Field: eventDate. Error: Должно содержать дату, которая еще не наступила. Value:" + dto.getEventDate());
        }
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new BadRequestException("Field: eventDate. Error: Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента. Value:" + dto.getEventDate());
        }
        Category category = categoryService.findById(dto.getCategory());
        User initiator = userService.findById(userId);
        Event event = EventMapper.toEntitySave(dto, category, initiator);
        if (dto.getPaid() == null) {
            event.setPaid(false);
        }
        if (dto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (dto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        event = eventRepository.save(event);
        EventFullDto fullDto = EventMapper.toFullDto(event);
        UserShortDto userShortDto = UserMapper.toDtoShort(initiator);
        fullDto.setInitiator(userShortDto);
        return fullDto;
    }

    public List<EventShortDto> getEventsFromUser(Long userId, int from, int size) {
        User initiator = userService.findById(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(initiator.getId(), pageable);
        return events.stream()
                .map(EventMapper::toShortDto)
                .peek(e -> e.setConfirmedRequests(requestRepository.countByEventIdAndStatus(e.getId(), Status.CONFIRMED)))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventById(Long userId, Long eventId) {
        User initiator = userService.findById(userId);
        eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + eventId + " was not found"));
        Event event = eventRepository.findByInitiatorIdAndId(initiator.getId(), eventId);
        return EventMapper.toFullDto(event);
    }

    public EventFullDto patchEvent(UpdateEventUserRequest dto, Long userId, Long eventId) {
        User initiator = userService.findById(userId);
        eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + eventId + " was not found"));
        Event event = eventRepository.findByInitiatorIdAndId(initiator.getId(), eventId);
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L)) && dto.getEventDate() != null) {
                throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента. dto:" + dto.getEventDate());
            }
        }
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }
        event = updateEventUserPriv(dto, event);
        EventFullDto fullDto = EventMapper.toFullDto(event);
        return fullDto;
    }

    private Event updateEventUserPriv(UpdateEventUserRequest dto, Event event) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            Category category = categoryService.findById(dto.getCategory());
            event.setCategory(category);
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getStateAction() != null && dto.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
            event.setState(State.PENDING);
        }
        if (dto.getStateAction() != null && dto.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
            event.setState(State.CANCELED);
        }
        event = eventRepository.save(event);
        return event;
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        userService.findById(userId);
        eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + eventId + " was not found"));
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult patchEventRequests(EventRequestStatusUpdateRequest dto, Long userId, Long eventId) {
        userService.findById(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + eventId + " was not found"));
        List<Request> requests = requestRepository.findByIdIn(dto.getRequestIds());
        Long countRequest = requestRepository.countByEventId(event.getId());
        if (event.getParticipantLimit() <= countRequest) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        // Подтверждение части заявок и часть отклонить, если заявок больше, чем доступно или отклонение всех заявок
        if (Status.CONFIRMED.equals(dto.getStatus()) && requests.size() > 0) {
            int cancelIndex = 0;
            int maxIndex = maxIndexResult(event.getParticipantLimit(), requests.size());
            for (int i = 0; i < maxIndex; i++) {
                requests.get(i).setStatus(Status.CONFIRMED);
                requestRepository.save(requests.get(i));
                cancelIndex++;
                confirmedRequests.add(RequestMapper.toDto(requests.get(i)));
            }
            for (int i = cancelIndex; i < requests.size(); i++) {
                checkStatus(requests.get(i));
                requests.get(i).setStatus(Status.REJECTED);
                requestRepository.save(requests.get(i));
                rejectedRequests.add(RequestMapper.toDto(requests.get(i)));
            }
        } else {
            for (int i = 0; i < requests.size(); i++) {
                checkStatus(requests.get(i));
                requests.get(i).setStatus(Status.REJECTED);
                requestRepository.save(requests.get(i));
                rejectedRequests.add(RequestMapper.toDto(requests.get(i)));
            }
        }
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    private void checkStatus(Request request) {
        if (request.getStatus().equals(Status.CONFIRMED)) {
            throw new ConflictException("Cтатус можно изменить только у заявок, находящихся в состоянии ожидания");
        }
    }

    private int maxIndexResult(Long a, int b) {
        int maxIndex;
        if (a <= b) {
            maxIndex = a.intValue();
        } else {
            maxIndex = b;
        }
        return maxIndex;
    }
}
