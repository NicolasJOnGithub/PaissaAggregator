package com.paissa.aggregator.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaissaOpenPlotDto(
        @JsonProperty("world_id") Integer worldId,
        @JsonProperty("district_id") Integer districtId,
        @JsonProperty("ward_number") Integer wardNumber,
        @JsonProperty("plot_number") Integer plotNumber,
        Integer size,
        Long price,
        @JsonProperty("purchase_system") Integer purchaseSystem,
        @JsonProperty("lotto_entries") Integer lottoEntries,
        @JsonProperty("lotto_phase") Integer lottoPhase,
        @JsonProperty("lotto_phase_until") Long lottoPhaseUntil,
        @JsonProperty("first_seen_time") Double firstSeenTime,
        @JsonProperty("last_updated_time") Double lastUpdatedTime) {}
