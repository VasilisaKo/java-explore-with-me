package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import model.ViewStatsDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.request.model.Status;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventAdminService {
    private final EventRepository eventRepository;

    private final CategoryService categoryService;

    private final UserService userService;

    private final RequestRepository requestRepository;

    private final StatClient statClient;

    public List<EventFullDto> getEvent(List<Long> users, List<String> states, List<Long> categories, String rangeStart,
                                       String rangeEnd, int from, int size) {
        List<State> statesEnum = new ArrayList<>();
        if (states != null) {
            for (String state : states) {
                statesEnum.add(State.valueOf(state));
            }
        } else {
            statesEnum = Arrays.asList(State.values());
        }
        LocalDateTime dateStartSearch = LocalDateTime.now().plusYears(99L);
        LocalDateTime dateEndSearch = LocalDateTime.now().minusYears(99L);
        if (rangeStart != null) {
            dateStartSearch = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeEnd != null) {
            dateEndSearch = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (users == null || users.size() == 0) {
            users = userService.findAll().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        }
        if (categories == null || categories.size() == 0) {
            categories = categoryService.findAll().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBefore(users, statesEnum, categories, dateStartSearch, dateEndSearch, pageable);
        if (events.isEmpty()) {
            events.addAll(eventRepository.findAllBy(PageRequest.of(from, size)));
        }
        return events.stream()
                .map(EventMapper::toFullDto)
                .peek(e -> e.setConfirmedRequests(requestRepository.countByEventIdAndStatus(e.getId(), Status.CONFIRMED)))
                .peek(e -> e.setViews(viewsEvent(rangeStart, rangeEnd, "/events/" + e.getId(), false)))
                .collect(Collectors.toList());
    }

    private Long viewsEvent(String rangeStart, String rangeEnd, String uris, Boolean unique) {
        List<ViewStatsDto> dto = statClient.getStat(rangeStart, rangeEnd, List.of(uris), unique);
        return dto.size() > 0 ? dto.get(0).getHits() : 0L;
    }

    public EventFullDto patchEvent(UpdateEventAdminRequest dto, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + eventId + " was not found"));
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1L))) {
                throw new BadRequestException("Field: eventDate. Error: the date should be in the future Value:" + dto.getEventDate());
            }
        }
        if (event.getState().equals(State.PUBLISHED) || event.getState().equals(State.CANCELED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        event = updateEvent(dto, event);
        EventFullDto fullDto = EventMapper.toFullDto(event);
        fullDto.setViews(viewsEvent(null, null, "/events/" + eventId, false));
        return fullDto;
    }

    private Event updateEvent(UpdateEventAdminRequest dto, Event event) {
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            Category categoryDto = categoryService.findById(dto.getCategory());
            event.setCategory(categoryDto);
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
        if (dto.getStateAction() != null && dto.getStateAction().equals(StateAction.PUBLISH_EVENT) && event.getState().equals(State.PENDING)) {
            event.setState(State.PUBLISHED);
        }
        if (dto.getStateAction() != null && dto.getStateAction().equals(StateAction.REJECT_EVENT) && !event.getState().equals(State.PUBLISHED)) {
            event.setState(State.CANCELED);
        }
        event = eventRepository.save(event);
        return event;
    }
}

