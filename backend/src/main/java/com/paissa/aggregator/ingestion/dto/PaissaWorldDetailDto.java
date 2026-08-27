package com.paissa.aggregator.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaissaWorldDetailDto(
        Integer id,
        String name,
        List<PaissaDistrictDto> districts,
        @JsonProperty("num_open_plots") Integer numOpenPlots,
        @JsonProperty("oldest_plot_time") Double oldestPlotTime) {}
