package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.Plot;
import com.paissa.aggregator.housing.PlotSize;
import com.paissa.aggregator.housing.PurchaseSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final PlotQueryRepository plotQueryRepository;

    public List<DatacenterSummary> datacenterSummaries() {
        Map<Integer, MutableCounts> byDatacenter = new LinkedHashMap<>();
        Map<Integer, String> names = new LinkedHashMap<>();

        for (DatacenterSizeCountRow row : plotQueryRepository.countAllByDatacenterAndSize()) {
            names.putIfAbsent(row.datacenterId(), row.datacenterName());
            byDatacenter.computeIfAbsent(row.datacenterId(), id -> new MutableCounts()).add(row.size(), row.count());
        }

        List<DatacenterSummary> summaries = new ArrayList<>();
        byDatacenter.forEach((id, counts) -> summaries.add(new DatacenterSummary(
                id, names.get(id), counts.small, counts.medium, counts.large, counts.total())));
        summaries.sort(Comparator.comparingLong(DatacenterSummary::totalCount).reversed());
        return summaries;
    }

    /**
     * Ranked worlds under the given ownership tab (defaults to counting every ownership type when
     * {@code ownership} is null), optionally scoped to one datacenter and/or one district. Every row
     * carries the full small/medium/large breakdown; {@code rankBy} only selects which column worlds
     * are sorted by.
     */
    public List<WorldStats> worldLeaderboard(
            PlotSize rankBy, PurchaseSystem ownership, Integer datacenterId, Integer districtId) {
        List<Integer> purchaseSystemCodes = ownership != null ? ownership.rawCodesForTab() : allRawCodes();

        Map<Integer, MutableCounts> countsByWorld = new LinkedHashMap<>();
        Map<Integer, WorldIdentity> identities = new LinkedHashMap<>();

        for (WorldSizeCountRow row : plotQueryRepository.countByWorldAndSize(purchaseSystemCodes, datacenterId, districtId)) {
            identities.putIfAbsent(row.worldId(), new WorldIdentity(row.worldName(), row.datacenterId(), row.datacenterName()));
            countsByWorld.computeIfAbsent(row.worldId(), id -> new MutableCounts()).add(row.size(), row.count());
        }

        List<WorldStats> stats = new ArrayList<>();
        countsByWorld.forEach((worldId, counts) -> {
            WorldIdentity identity = identities.get(worldId);
            stats.add(new WorldStats(
                    worldId,
                    identity.name(),
                    identity.datacenterId(),
                    identity.datacenterName(),
                    counts.small,
                    counts.medium,
                    counts.large,
                    counts.total()));
        });

        Comparator<WorldStats> byRankColumn = rankBy == null
                ? Comparator.comparingLong(WorldStats::totalCount)
                : switch (rankBy) {
                    case SMALL -> Comparator.comparingLong(WorldStats::smallCount);
                    case MEDIUM -> Comparator.comparingLong(WorldStats::mediumCount);
                    case LARGE -> Comparator.comparingLong(WorldStats::largeCount);
                };
        stats.sort(byRankColumn.reversed());
        return stats;
    }

    public Page<Plot> worldPlots(
            Integer worldId, PlotSize size, PurchaseSystem ownership, Integer districtId, Pageable pageable) {
        List<Integer> purchaseSystemCodes = ownership != null ? ownership.rawCodesForTab() : allRawCodes();
        return size != null
                ? plotQueryRepository.findWorldPlotsBySize(worldId, size, purchaseSystemCodes, districtId, pageable)
                : plotQueryRepository.findWorldPlots(worldId, purchaseSystemCodes, districtId, pageable);
    }

    /** Unfiltered small/medium/large breakdown for one world (0s if it hasn't been synced yet). */
    public SizeCounts worldSizeCounts(Integer worldId) {
        MutableCounts counts = new MutableCounts();
        for (WorldSizeCountRow row : plotQueryRepository.countByWorldIdAndSize(worldId)) {
            counts.add(row.size(), row.count());
        }
        return new SizeCounts(counts.small, counts.medium, counts.large);
    }

    private static List<Integer> allRawCodes() {
        return List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    private record WorldIdentity(String name, Integer datacenterId, String datacenterName) {}

    private static final class MutableCounts {
        private long small;
        private long medium;
        private long large;

        void add(PlotSize size, long count) {
            switch (size) {
                case SMALL -> small += count;
                case MEDIUM -> medium += count;
                case LARGE -> large += count;
            }
        }

        long total() {
            return small + medium + large;
        }
    }
}
