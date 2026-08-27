package com.paissa.aggregator.ingestion;

import com.paissa.aggregator.ingestion.dto.PaissaWorldDetailDto;
import com.paissa.aggregator.ingestion.dto.PaissaWorldDto;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * PAISSADB enforces a tight rate limit (observed: a couple of requests before a 429, with a
 * {@code Retry-After} header telling us how long to back off). We sync 85 worlds per refresh, so
 * rate limiting is the normal case, not an edge case — every call waits out the server's own
 * Retry-After hint instead of guessing at backoff timing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaissaApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RATE_LIMIT_ATTEMPTS = 8;
    private static final long DEFAULT_RETRY_AFTER_SECONDS = 10;

    private final WebClient paissaWebClient;

    @Retryable(retryFor = WebClientRequestException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<PaissaWorldDto> fetchWorlds() {
        return callWithRateLimitHandling(
                "GET /worlds",
                () -> paissaWebClient
                        .get()
                        .uri("/worlds")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<PaissaWorldDto>>() {})
                        .block(TIMEOUT));
    }

    @Retryable(retryFor = WebClientRequestException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public PaissaWorldDetailDto fetchWorldDetail(Integer worldId) {
        return callWithRateLimitHandling(
                "GET /worlds/" + worldId,
                () -> paissaWebClient
                        .get()
                        .uri("/worlds/{id}", worldId)
                        .retrieve()
                        .bodyToMono(PaissaWorldDetailDto.class)
                        .block(TIMEOUT));
    }

    private <T> T callWithRateLimitHandling(String description, Supplier<T> call) {
        for (int attempt = 1; attempt <= MAX_RATE_LIMIT_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (WebClientResponseException.TooManyRequests e) {
                if (attempt == MAX_RATE_LIMIT_ATTEMPTS) {
                    throw e;
                }
                long waitSeconds = retryAfterSeconds(e.getHeaders());
                log.warn(
                        "{} rate limited (attempt {}/{}), waiting {}s before retry",
                        description,
                        attempt,
                        MAX_RATE_LIMIT_ATTEMPTS,
                        waitSeconds);
                sleepSeconds(waitSeconds);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private static long retryAfterSeconds(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        try {
            return Math.max(1, Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }

    private static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting out PAISSADB rate limit", e);
        }
    }
}
