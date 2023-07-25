package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Event findByInitiatorIdAndId(Long initiatorId, Long eventId);

    List<Event> findAllByInitiatorIdInAndStateInAndCategoryIdInAndEventDateIsAfterAndEventDateIsBefore(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (lower(e.annotation) LIKE lower(concat('%', :text, '%')) OR lower(e.description) LIKE lower(concat('%', :text, '%')) OR :text IS NULL) " +
            "AND (e.category.id IN :categories OR :categories IS NULL) " +
            "AND (e.paid=:paid OR :paid IS NULL) " +
            "AND (e.eventDate BETWEEN :rangeStart AND :rangeEnd) " +
            "AND (e.state = :state) " +
            "ORDER BY e.eventDate")
    List<Event> searchEventPub(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, State state, Pageable pageable);

    @Query("SELECT e.id " +
            "FROM Event e ")
    List<Long> findAllIds();

    List<Event> findAllByCategoryId(Long catId);

    List<Event> findAllBy(PageRequest pageRequest);
}
