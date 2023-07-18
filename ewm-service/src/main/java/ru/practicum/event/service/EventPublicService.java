package ru.practicum.event.service;

import lombok.AllArgsConstructor;
import model.EndpointHitDto;
import model.ViewStatsDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.request.model.Status;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventPublicService {
    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final RequestRepository requestRepository;

    private final StatClient statClient;


    public EventFullDto getEvent(Long id, String ip) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + id + " was not found"));
        if (event.getState() != State.PUBLISHED) {
            throw new ObjectNotFoundException("The event hasn't been published");
        }
        EventFullDto fullDto = EventMapper.toFullDto(event);
        saveHit(ip, id);
        if (fullDto.getViews() == null) {
            fullDto.setViews(1L);
        } else {
            fullDto.setViews(fullDto.getViews() + 1);
        }
        return fullDto;
    }

    @Transactional
    public List<EventShortDto> getSearchEvent(String text, List<Long> categories, Boolean paid, String rangeStart,
                                              String rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
                                              String ip) {
        LocalDateTime dateStartSearch = LocalDateTime.now().plusSeconds(1L);
        LocalDateTime dateEndSearch = LocalDateTime.now().plusYears(99L);
        if (rangeStart != null) {
            dateStartSearch = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeEnd != null) {
            dateEndSearch = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (categories == null || categories.size() == 0) {
            categories = categoryRepository.findAll().stream()
                    .map(c -> c.getId())
                    .collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.searchEventPub(text, categories, paid, dateStartSearch, dateEndSearch, State.PUBLISHED, pageable);
        // Только события у которых не исчерпан лимит запросов на участие
        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() > confirmedRequests(e.getId()))
                    .collect(Collectors.toList());
        }
        List<EventShortDto> eventShorts = events.stream()
                .map(EventMapper::toShortDto)
                .peek(e -> e.setViews(viewsEvent(rangeStart, rangeEnd, "/events/" + e.getId(), false)))
                .collect(Collectors.toList());
        // Вариант сортировки по количеству просмотров
        if (sort.equals("VIEWS")) {
            eventShorts.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews));
        }
        saveHit(ip, null);
        if (eventShorts.isEmpty()) {
            throw new BadRequestException("Wrong request");
        }
        return eventShorts;
    }

    public Event findById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Event with id=" + id + " was not found"));
    }

    public List<Event> findEventsById(List<Long> events) {
        return eventRepository.findAllById(events);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public List<Long> findAllIds() {
        return eventRepository.findAllIds();
    }

    public void deleteCategoryFromEvent(Long catId) {
        List<Event> events = eventRepository.findAllByCategoryId(catId);
        if (!events.isEmpty()) {
            throw new ConflictException("This category is in use in event");
        }
    }


    private Long confirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
    }

    private Long viewsEvent(String rangeStart, String rangeEnd, String uris, Boolean unique) {
        List<ViewStatsDto> dto = statClient.getStat(rangeStart, rangeEnd, List.of(uris), unique);
        return dto.size() > 0 ? dto.get(0).getHits() : 0L;
    }

    private void saveHit(String ip, Long eventId) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-service");
        endpointHitDto.setTimestamp(LocalDateTime.now());
        endpointHitDto.setIp(ip);
        if (eventId == null) {
            endpointHitDto.setUri("/events");
        } else {
            endpointHitDto.setUri("/events/" + eventId);
        }
        statClient.saveStat(endpointHitDto);
    }
}