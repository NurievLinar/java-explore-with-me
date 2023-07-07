package ru.practicum.ewm.stat.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.stat.dto.HitDto;
import ru.practicum.ewm.stat.dto.StatDto;

import java.util.List;
import java.util.Map;

@Service
public class StatClient extends BaseClient {

    @Autowired
    public StatClient(@Value("${stat-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createHit(HitDto hitDto) {
        return post("/hit", hitDto);
    }

    public List<StatDto> getStat(String start, String end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", String.join(",", uris),
                "unique", unique
        );
        ResponseEntity<Object> objectResponseEntity =
                get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
        List<StatDto> statDto = new ObjectMapper().convertValue(objectResponseEntity.getBody(), new TypeReference<>() {
        });
        return statDto;
    }
}
