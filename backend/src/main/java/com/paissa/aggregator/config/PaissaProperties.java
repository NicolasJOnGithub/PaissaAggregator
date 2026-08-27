package com.paissa.aggregator.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paissa")
public record PaissaProperties(Api api, Refresh refresh, Cors cors) {

    public record Api(String baseUrl) {}

    public record Refresh(String key, long fixedDelayMs, long initialDelayMs, long requestPacingMs) {}

    public record Cors(List<String> allowedOrigins) {}
}
