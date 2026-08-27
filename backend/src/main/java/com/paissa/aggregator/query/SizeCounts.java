package com.paissa.aggregator.query;

public record SizeCounts(long small, long medium, long large) {

    public static final SizeCounts ZERO = new SizeCounts(0, 0, 0);

    public long total() {
        return small + medium + large;
    }
}
