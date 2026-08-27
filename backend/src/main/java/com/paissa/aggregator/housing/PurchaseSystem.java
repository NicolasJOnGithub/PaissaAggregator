package com.paissa.aggregator.housing;

import java.util.Arrays;
import java.util.List;

/**
 * Normalized ownership category derived from PAISSADB's raw {@code purchase_system} code.
 * Plots are stored with their raw code (1-9); this enum is a query/API-time view over it.
 */
public enum PurchaseSystem {
    FC_ONLY(List.of(1, 4, 7)),
    INDIVIDUAL_ONLY(List.of(2, 5, 8)),
    UNRESTRICTED(List.of(3, 6, 9));

    private final List<Integer> rawCodes;

    PurchaseSystem(List<Integer> rawCodes) {
        this.rawCodes = rawCodes;
    }

    public List<Integer> rawCodes() {
        return rawCodes;
    }

    public static PurchaseSystem fromRawCode(int rawCode) {
        return Arrays.stream(values())
                .filter(p -> p.rawCodes.contains(rawCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown purchase_system code: " + rawCode));
    }

    /**
     * Raw codes visible on this ownership tab. UNRESTRICTED plots are purchasable by anyone, so they
     * show up on the FC-only and Individual-only tabs too; the FC-only/Individual-only tabs' own codes
     * only ever show on their own tab.
     */
    public List<Integer> rawCodesForTab() {
        return switch (this) {
            case FC_ONLY -> concat(FC_ONLY, UNRESTRICTED);
            case INDIVIDUAL_ONLY -> concat(INDIVIDUAL_ONLY, UNRESTRICTED);
            case UNRESTRICTED -> UNRESTRICTED.rawCodes;
        };
    }

    private static List<Integer> concat(PurchaseSystem a, PurchaseSystem b) {
        List<Integer> codes = new java.util.ArrayList<>(a.rawCodes);
        codes.addAll(b.rawCodes);
        return codes;
    }
}
