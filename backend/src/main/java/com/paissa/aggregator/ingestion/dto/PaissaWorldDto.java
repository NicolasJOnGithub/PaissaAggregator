package com.paissa.aggregator.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaissaWorldDto(
        Integer id,
        String name,
        @JsonProperty("datacenter_id") Integer datacenterId,
        @JsonProperty("datacenter_name") String datacenterName) {}
