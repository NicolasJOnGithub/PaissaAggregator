package com.paissa.aggregator.ingestion;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_status")
@Getter
@Setter
@NoArgsConstructor
public class RefreshStatus {

    @Id
    private Integer id = 1;

    private Instant lastStartedAt;

    private Instant lastCompletedAt;

    private boolean inProgress;

    private Integer worldsSynced;

    private String lastError;
}
