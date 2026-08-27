package com.paissa.aggregator.world;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldRepository extends JpaRepository<World, Integer> {

    @EntityGraph(attributePaths = "datacenter")
    List<World> findAllByOrderByNameAsc();

    @Override
    @EntityGraph(attributePaths = "datacenter")
    Optional<World> findById(Integer id);
}
