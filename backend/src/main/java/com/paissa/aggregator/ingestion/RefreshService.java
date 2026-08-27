package com.paissa.aggregator.ingestion;

import com.paissa.aggregator.config.PaissaProperties;
import com.paissa.aggregator.housing.PlotService;
import com.paissa.aggregator.ingestion.dto.PaissaDistrictDto;
import com.paissa.aggregator.ingestion.dto.PaissaWorldDetailDto;
import com.paissa.aggregator.ingestion.dto.PaissaWorldDto;
import com.paissa.aggregator.world.District;
import com.paissa.aggregator.world.World;
import com.paissa.aggregator.world.WorldRepository;
import com.paissa.aggregator.world.WorldService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshService {

    private final PaissaApiClient paissaApiClient;
    private final WorldService worldService;
    private final WorldRepository worldRepository;
    private final PlotService plotService;
    private final RefreshStatusRepository refreshStatusRepository;
    private final PaissaProperties properties;

    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Full resync of every world from PAISSADB. No-ops if a refresh is already running. Runs off the caller's thread. */
    @Async
    public void refreshAll() {
        if (!running.compareAndSet(false, true)) {
            log.info("Refresh already in progress, skipping this trigger.");
            return;
        }
        try {
            markStarted();
            List<PaissaWorldDto> worlds = paissaApiClient.fetchWorlds();
            worldService.upsertWorlds(worlds.stream()
                    .map(w -> new WorldService.WorldMasterData(w.id(), w.name(), w.datacenterId(), w.datacenterName()))
                    .toList());

            int synced = 0;
            for (PaissaWorldDto worldDto : worlds) {
                try {
                    syncWorld(worldDto.id());
                    synced++;
                } catch (Exception e) {
                    log.error("Failed to sync world {} ({})", worldDto.id(), worldDto.name(), e);
                }
                pace();
            }
            markCompleted(synced, null);
        } catch (Exception e) {
            log.error("Refresh aborted", e);
            markCompleted(0, e.getMessage());
        } finally {
            running.set(false);
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    /** Light proactive throttle between per-world requests, on top of the client's own Retry-After handling. */
    private void pace() {
        try {
            Thread.sleep(properties.refresh().requestPacingMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void syncWorld(Integer worldId) {
        PaissaWorldDetailDto detail = paissaApiClient.fetchWorldDetail(worldId);
        World world = worldRepository
                .findById(worldId)
                .orElseThrow(() -> new IllegalStateException("World " + worldId + " not found after master-data upsert"));

        List<PlotService.DistrictOpenPlots> districtOpenPlots = new ArrayList<>();
        for (PaissaDistrictDto districtDto : detail.districts()) {
            District district = worldService.upsertDistrict(districtDto.id(), districtDto.name());
            List<PlotService.PlotInput> plots = districtDto.openPlots().stream()
                    .map(p -> new PlotService.PlotInput(
                            p.wardNumber(),
                            p.plotNumber(),
                            p.size(),
                            p.price(),
                            p.purchaseSystem(),
                            p.lottoEntries(),
                            p.lottoPhase(),
                            p.lottoPhaseUntil(),
                            p.firstSeenTime(),
                            p.lastUpdatedTime()))
                    .toList();
            districtOpenPlots.add(new PlotService.DistrictOpenPlots(district, plots));
        }
        plotService.replaceWorldHousingData(world, districtOpenPlots);
    }

    private void markStarted() {
        RefreshStatus status = refreshStatusRepository.findById(1).orElseGet(RefreshStatus::new);
        status.setInProgress(true);
        status.setLastStartedAt(Instant.now());
        refreshStatusRepository.save(status);
    }

    private void markCompleted(int worldsSynced, String error) {
        RefreshStatus status = refreshStatusRepository.findById(1).orElseGet(RefreshStatus::new);
        status.setInProgress(false);
        status.setLastCompletedAt(Instant.now());
        status.setWorldsSynced(worldsSynced);
        status.setLastError(error);
        refreshStatusRepository.save(status);
    }
}
