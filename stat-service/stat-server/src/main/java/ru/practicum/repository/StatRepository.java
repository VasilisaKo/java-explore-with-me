package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import model.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new model.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp >= ?1 " +
            "AND e.timestamp <= ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getUniqueStats(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT new model.ViewStatsDto(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp >= ?1 " +
            "AND e.timestamp <= ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getStats(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT new model.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp >= ?1 " +
            "AND e.timestamp <= ?2 " +
            "AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getUniqueStatsUri(LocalDateTime startTime, LocalDateTime endTime, List<String> uris);

    @Query("SELECT new model.ViewStatsDto(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp >= ?1 " +
            "AND e.timestamp <= ?2 " +
            "AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getStatsUri(LocalDateTime startTime, LocalDateTime endTime, List<String> uris);
}
