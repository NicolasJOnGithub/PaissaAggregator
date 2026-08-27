package com.paissa.aggregator.world;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final DatacenterRepository datacenterRepository;
    private final WorldRepository worldRepository;
    private final DistrictRepository districtRepository;

    /** Upserts datacenter + world master data. Safe to call repeatedly (idempotent). */
    @Transactional
    public void upsertWorlds(List<WorldMasterData> worldsData) {
        Map<Integer, Datacenter> datacenters = new java.util.HashMap<>();
        for (WorldMasterData w : worldsData) {
            datacenters.computeIfAbsent(w.datacenterId(), id -> {
                Datacenter dc = datacenterRepository.findById(id).orElseGet(Datacenter::new);
                dc.setId(id);
                dc.setName(w.datacenterName());
                return datacenterRepository.save(dc);
            });
        }

        for (WorldMasterData w : worldsData) {
            World world = worldRepository.findById(w.id()).orElseGet(World::new);
            world.setId(w.id());
            world.setName(w.name());
            world.setDatacenter(datacenters.get(w.datacenterId()));
            worldRepository.save(world);
        }
    }

    /** Upserts a district (shared, global lookup keyed by PAISSADB district id). */
    @Transactional
    public District upsertDistrict(Integer id, String name) {
        District district = districtRepository.findById(id).orElseGet(District::new);
        district.setId(id);
        district.setName(name);
        return districtRepository.save(district);
    }

    public record WorldMasterData(Integer id, String name, Integer datacenterId, String datacenterName) {}
}
