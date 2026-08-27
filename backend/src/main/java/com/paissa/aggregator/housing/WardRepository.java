package com.paissa.aggregator.housing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByWorldIdAndDistrictIdAndWardNumber(Integer worldId, Integer districtId, Integer wardNumber);

    @Modifying
    @Query("delete from Ward w where w.world.id = :worldId")
    void deleteAllByWorldId(@Param("worldId") Integer worldId);
}
