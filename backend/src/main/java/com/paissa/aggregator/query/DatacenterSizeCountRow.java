package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.PlotSize;

/** Internal JPQL projection row: one (datacenter, size) count. Aggregated into {@link DatacenterSummary} by QueryService. */
public record DatacenterSizeCountRow(Integer datacenterId, String datacenterName, PlotSize size, Long count) {}
