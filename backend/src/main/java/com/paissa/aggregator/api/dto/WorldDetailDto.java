package com.paissa.aggregator.api.dto;

public record WorldDetailDto(
        Integer id,
        String name,
        Integer datacenterId,
        String datacenterName,
        long smallCount,
        long mediumCount,
        long largeCount,
        long totalCount) {}
