package com.paissa.aggregator.world;

import java.util.Map;

/**
 * FF XIV's 11 datacenters are grouped into 4 regions. PAISSADB doesn't expose this grouping (it
 * only ever returns datacenter id/name) — it's the game's own long-stable list, hardcoded here.
 */
public final class DatacenterRegions {

    private static final Map<String, Region> BY_NAME = Map.ofEntries(
            Map.entry("Elemental", Region.JAPAN),
            Map.entry("Gaia", Region.JAPAN),
            Map.entry("Mana", Region.JAPAN),
            Map.entry("Meteor", Region.JAPAN),
            Map.entry("Aether", Region.NORTH_AMERICA),
            Map.entry("Primal", Region.NORTH_AMERICA),
            Map.entry("Crystal", Region.NORTH_AMERICA),
            Map.entry("Dynamis", Region.NORTH_AMERICA),
            Map.entry("Chaos", Region.EUROPE),
            Map.entry("Light", Region.EUROPE),
            Map.entry("Materia", Region.OCEANIA));

    private DatacenterRegions() {}

    /** Null if the name isn't one of the known 11 datacenters (e.g. PAISSADB adds a new one). */
    public static Region of(String datacenterName) {
        return BY_NAME.get(datacenterName);
    }
}
