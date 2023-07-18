package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.EndpointHit;
import model.ViewStatsDto;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatRepository statRepository;

    public EndpointHit saveEndpointHit(EndpointHit endpointHit) {
        return statRepository.save(endpointHit);
    }

    public List<ViewStatsDto> getViewStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null) {
            if (unique) {
                return statRepository.getUniqueStats(start, end);
            } else {
                return statRepository.getStats(start, end);
            }
        } else {
            if (unique) {
                return statRepository.getUniqueStatsUri(start, end, uris);
            } else {
                List<ViewStatsDto> list = statRepository.getStatsUri(start, end, uris);
                return list;
            }
        }
    }
}
