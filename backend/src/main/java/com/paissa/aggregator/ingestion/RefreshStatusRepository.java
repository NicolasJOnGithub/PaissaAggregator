package com.paissa.aggregator.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshStatusRepository extends JpaRepository<RefreshStatus, Integer> {}
