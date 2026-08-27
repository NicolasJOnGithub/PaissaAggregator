package com.paissa.aggregator.api;

import com.paissa.aggregator.api.dto.RefreshStatusDto;
import com.paissa.aggregator.config.PaissaProperties;
import com.paissa.aggregator.ingestion.RefreshService;
import com.paissa.aggregator.ingestion.RefreshStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshController {

    private static final String REFRESH_KEY_HEADER = "X-Refresh-Key";

    private final RefreshService refreshService;
    private final RefreshStatusRepository refreshStatusRepository;
    private final PaissaProperties properties;

    @PostMapping("/api/refresh")
    public ResponseEntity<Void> triggerRefresh(@RequestHeader(value = REFRESH_KEY_HEADER, required = false) String key) {
        if (!properties.refresh().key().equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (refreshService.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        refreshService.refreshAll();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/api/refresh/status")
    public RefreshStatusDto status() {
        return refreshStatusRepository
                .findById(1)
                .map(s -> new RefreshStatusDto(
                        s.getLastStartedAt(), s.getLastCompletedAt(), s.isInProgress(), s.getWorldsSynced(), s.getLastError()))
                .orElseGet(() -> new RefreshStatusDto(null, null, false, null, null));
    }
}
