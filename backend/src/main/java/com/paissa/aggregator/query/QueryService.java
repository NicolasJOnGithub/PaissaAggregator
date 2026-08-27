package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.Plot;
import com.paissa.aggregator.housing.PlotSize;
import com.paissa.aggregator.housing.PurchaseSystem;
import com.paissa.aggregator.world.DatacenterRegions;
import com.paissa.aggregator.world.District;
import com.paissa.aggregator.world.DistrictRepository;
import com.paissa.aggregator.world.Region;
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
    private final DistrictRepository districtRepository;

    public List<DatacenterSummary> datacenterSummaries(Region region) {
        Map<Integer, MutableCounts> byDatacenter = new LinkedHashMap<>();
        Map<Integer, String> names = new LinkedHashMap<>();

        for (DatacenterSizeCountRow row : plotQueryRepository.countAllByDatacenterAndSize()) {
            names.putIfAbsent(row.datacenterId(), row.datacenterName());
            byDatacenter.computeIfAbsent(row.datacenterId(), id -> new MutableCounts()).add(row.size(), row.count());
        }

        List<DatacenterSummary> summaries = new ArrayList<>();
        byDatacenter.forEach((id, counts) -> {
            String name = names.get(id);
            if (region == null || DatacenterRegions.of(name) == region) {
                summaries.add(new DatacenterSummary(id, name, counts.small, counts.medium, counts.large, counts.total()));
            }
        });
        summaries.sort(Comparator.comparingLong(DatacenterSummary::totalCount).reversed());
        return summaries;
    }

    /**
     * Ranked worlds under the given ownership tab (defaults to counting every ownership type when
     * {@code ownership} is null), optionally scoped to a datacenter, a region, and/or a set of
     * districts. Every row always carries the full small/medium/large breakdown; {@code rankSizes}
     * only selects which sizes are summed for sorting (empty/null means rank by total).
     */
    public List<WorldStats> worldLeaderboard(
            List<PlotSize> rankSizes,
            PurchaseSystem ownership,
            Integer datacenterId,
            List<Integer> districtIds,
            Region region) {
        List<Integer> purchaseSystemCodes = ownership != null ? ownership.rawCodesForTab() : allRawCodes();

        Map<Integer, MutableCounts> countsByWorld = new LinkedHashMap<>();
        Map<Integer, WorldIdentity> identities = new LinkedHashMap<>();

        for (WorldSizeCountRow row :
                plotQueryRepository.countByWorldAndSize(purchaseSystemCodes, datacenterId, effectiveDistrictIds(districtIds))) {
            identities.putIfAbsent(row.worldId(), new WorldIdentity(row.worldName(), row.datacenterId(), row.datacenterName()));
            countsByWorld.computeIfAbsent(row.worldId(), id -> new MutableCounts()).add(row.size(), row.count());
        }

        List<WorldStats> stats = new ArrayList<>();
        countsByWorld.forEach((worldId, counts) -> {
            WorldIdentity identity = identities.get(worldId);
            if (region != null && DatacenterRegions.of(identity.datacenterName()) != region) {
                return;
            }
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

        Comparator<WorldStats> byRankColumn = (rankSizes == null || rankSizes.isEmpty())
                ? Comparator.comparingLong(WorldStats::totalCount)
                : Comparator.comparingLong(w -> sumSelectedSizes(w, rankSizes));
        stats.sort(byRankColumn.reversed());
        return stats;
    }

    public Page<Plot> worldPlots(
            Integer worldId,
            List<PlotSize> sizes,
            PurchaseSystem ownership,
            List<Integer> districtIds,
            Pageable pageable) {
        List<Integer> purchaseSystemCodes = ownership != null ? ownership.rawCodesForTab() : allRawCodes();
        return plotQueryRepository.findWorldPlots(
                worldId, effectiveSizes(sizes), purchaseSystemCodes, effectiveDistrictIds(districtIds), pageable);
    }

    /** Unfiltered small/medium/large breakdown for one world (0s if it hasn't been synced yet). */
    public SizeCounts worldSizeCounts(Integer worldId) {
        MutableCounts counts = new MutableCounts();
        for (WorldSizeCountRow row : plotQueryRepository.countByWorldIdAndSize(worldId)) {
            counts.add(row.size(), row.count());
        }
        return new SizeCounts(counts.small, counts.medium, counts.large);
    }

    private static long sumSelectedSizes(WorldStats stats, List<PlotSize> sizes) {
        long sum = 0;
        for (PlotSize size : sizes) {
            sum += switch (size) {
                case SMALL -> stats.smallCount();
                case MEDIUM -> stats.mediumCount();
                case LARGE -> stats.largeCount();
            };
        }
        return sum;
    }

    private static List<PlotSize> effectiveSizes(List<PlotSize> sizes) {
        return (sizes == null || sizes.isEmpty()) ? List.of(PlotSize.values()) : sizes;
    }

    /** No district filter is expressed as "every known district id" to keep the JPQL IN clause non-nullable. */
    private List<Integer> effectiveDistrictIds(List<Integer> districtIds) {
        if (districtIds != null && !districtIds.isEmpty()) {
            return districtIds;
        }
        return districtRepository.findAll().stream().map(District::getId).toList();
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
