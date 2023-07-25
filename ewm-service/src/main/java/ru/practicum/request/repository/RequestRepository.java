package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.Status;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Long countByEventIdAndStatus(Long eventId, Status status);

    Long countByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findByIdIn(List<Long> ids);

    List<Request> findAllByRequesterIdAndEventId(Long userId, Long eventId);
}