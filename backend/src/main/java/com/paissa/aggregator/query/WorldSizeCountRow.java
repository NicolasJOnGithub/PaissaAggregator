package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.PlotSize;

/** Internal JPQL projection row: one (world, size) count under a given ownership filter. Aggregated into {@link WorldStats}. */
public record WorldSizeCountRow(
        Integer worldId, String worldName, Integer datacenterId, String datacenterName, PlotSize size, Long count) {}
