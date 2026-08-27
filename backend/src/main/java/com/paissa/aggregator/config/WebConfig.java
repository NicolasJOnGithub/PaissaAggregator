package com.paissa.aggregator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(PaissaProperties.class)
public class WebConfig {

    @Bean
    public WebClient paissaWebClient(PaissaProperties properties) {
        // Busy worlds' /worlds/{id} payload (all districts + open plots) comfortably exceeds
        // WebClient's default 256KB in-memory buffer, so raise it well past any realistic size.
        return WebClient.builder()
                .baseUrl(properties.api().baseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(PaissaProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/**")
                        .allowedOrigins(properties.cors().allowedOrigins().toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
