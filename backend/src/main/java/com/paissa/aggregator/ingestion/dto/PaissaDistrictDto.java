package com.paissa.aggregator.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaissaDistrictDto(
        Integer id,
        String name,
        @JsonProperty("num_open_plots") Integer numOpenPlots,
        @JsonProperty("oldest_plot_time") Double oldestPlotTime,
        @JsonProperty("open_plots") List<PaissaOpenPlotDto> openPlots) {}
