package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class StatsClient {
    private static final ParameterizedTypeReference<List<ViewStatsDto>> VIEW_STATS_LIST_REFERENCE =
            new ParameterizedTypeReference<List<ViewStatsDto>>() {};

    private final WebClient webClient;

    public StatsClient(@Value("${stats-server.url}") String statsServerUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(statsServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void saveHitAsync(String app, String uri, String ip, LocalDateTime timestamp) {
        Mono<EndpointHitDto> monoResponse = webClient.post()
                .uri("/hit")
                .bodyValue(EndpointHitDto.builder().app(app).uri(uri).ip(ip).timestamp(timestamp).build())
                .retrieve()
                .bodyToMono(EndpointHitDto.class);
        monoResponse.subscribe(endpointHit -> log.info("Hit was send: " + endpointHit));
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Mono<List<ViewStatsDto>> monoResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start.format(DateTimeFormatter.ofPattern(EndpointHitDto.DATE_TIME_FORMAT)))
                        .queryParam("end", end.format(DateTimeFormatter.ofPattern(EndpointHitDto.DATE_TIME_FORMAT)))
                        .queryParamIfPresent("uris", Optional.of(uris))
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToMono(VIEW_STATS_LIST_REFERENCE);

        return monoResponse.block();

    }

}
