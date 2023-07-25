package ru.practicum.client;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.ViewStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import model.EndpointHitDto;

import java.util.List;
import java.util.Map;

@Service
public class StatClient extends BaseClient {

    private static final String START_DEFAULT = "1900-01-01 01:01:01";
    private static final String END_DEFAULT = "2200-01-01 01:01:01";

    @Autowired
    public StatClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> saveStat(EndpointHitDto dto) {
        return post("/hit", dto);
    }

    public List<ViewStatsDto> getStat(String start, String end, List<String> uris, boolean unique) {
        if (start == null) {
            start = START_DEFAULT;
        }
        if (end == null) {
            end = END_DEFAULT;
        }
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uris,
                "unique", unique
        );
        ResponseEntity<Object> objectResponseEntity = get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
        List<ViewStatsDto> viewStatsDto = new ObjectMapper().convertValue(objectResponseEntity.getBody(), new TypeReference<List<ViewStatsDto>>() {
        });
        return viewStatsDto;
    }
}