package com.paissa.aggregator.query;

public record WorldStats(
        Integer worldId,
        String worldName,
        Integer datacenterId,
        String datacenterName,
        long smallCount,
        long mediumCount,
        long largeCount,
        long totalCount) {}
