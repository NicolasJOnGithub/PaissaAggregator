package com.paissa.aggregator.api.dto;

public record PlotDto(
        Long id,
        Integer worldId,
        String worldName,
        Integer districtId,
        String districtName,
        Integer wardNumber,
        Integer plotNumber,
        String size,
        Long price,
        String ownership,
        Integer lottoEntries,
        Integer lottoPhase,
        Long lottoPhaseUntil,
        Double firstSeenTime,
        Double lastUpdatedTime) {}
