package com.paissa.aggregator.query;

public record DatacenterSummary(
        Integer datacenterId, String datacenterName, long smallCount, long mediumCount, long largeCount, long totalCount) {}
