package com.paissa.aggregator.api.dto;

import java.time.Instant;

public record RefreshStatusDto(
        Instant lastStartedAt, Instant lastCompletedAt, boolean inProgress, Integer worldsSynced, String lastError) {}
