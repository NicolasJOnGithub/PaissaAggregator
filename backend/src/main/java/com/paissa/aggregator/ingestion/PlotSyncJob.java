package com.paissa.aggregator.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlotSyncJob {

    private final RefreshService refreshService;

    @Scheduled(
            initialDelayString = "${paissa.refresh.initial-delay-ms}",
            fixedDelayString = "${paissa.refresh.fixed-delay-ms}")
    public void syncOnSchedule() {
        log.info("Scheduled PAISSADB sync starting");
        refreshService.refreshAll();
    }
}
