package com.paissa.aggregator.housing;

import java.util.Arrays;

public enum PlotSize {
    SMALL(0),
    MEDIUM(1),
    LARGE(2);

    private final int code;

    PlotSize(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static PlotSize fromCode(int code) {
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown plot size code: " + code));
    }
}
