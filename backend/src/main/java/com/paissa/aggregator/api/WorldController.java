package com.paissa.aggregator.api;

import com.paissa.aggregator.api.dto.DistrictDto;
import com.paissa.aggregator.api.dto.PlotDto;
import com.paissa.aggregator.api.dto.WorldDetailDto;
import com.paissa.aggregator.api.dto.WorldDto;
import com.paissa.aggregator.housing.Plot;
import com.paissa.aggregator.housing.PlotSize;
import com.paissa.aggregator.housing.PurchaseSystem;
import com.paissa.aggregator.query.QueryService;
import com.paissa.aggregator.query.SizeCounts;
import com.paissa.aggregator.world.DistrictRepository;
import com.paissa.aggregator.world.World;
import com.paissa.aggregator.world.WorldRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorldController {

    private final WorldRepository worldRepository;
    private final DistrictRepository districtRepository;
    private final QueryService queryService;
    private final PlotMapper plotMapper;

    @GetMapping("/api/worlds")
    public List<WorldDto> listWorlds() {
        return worldRepository.findAllByOrderByNameAsc().stream()
                .map(w -> new WorldDto(w.getId(), w.getName(), w.getDatacenter().getId(), w.getDatacenter().getName()))
                .toList();
    }

    @GetMapping("/api/districts")
    public List<DistrictDto> listDistricts() {
        return districtRepository.findAllByOrderByIdAsc().stream()
                .map(d -> new DistrictDto(d.getId(), d.getName()))
                .toList();
    }

    @GetMapping("/api/worlds/{id}")
    public ResponseEntity<WorldDetailDto> worldDetail(@PathVariable Integer id) {
        return worldRepository
                .findById(id)
                .map(world -> ResponseEntity.ok(toDetailDto(world, queryService.worldSizeCounts(id))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/worlds/{id}/plots")
    public ResponseEntity<PagedModel<PlotDto>> worldPlots(
            @PathVariable Integer id,
            @RequestParam(required = false) PlotSize size,
            @RequestParam(required = false) PurchaseSystem ownership,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        if (worldRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Pageable pageable = PageRequest.of(page, Math.min(pageSize, 200));
        Page<Plot> plots = queryService.worldPlots(id, size, ownership, districtId, pageable);
        return ResponseEntity.ok(new PagedModel<>(plots.map(plotMapper::toDto)));
    }

    private WorldDetailDto toDetailDto(World world, SizeCounts counts) {
        return new WorldDetailDto(
                world.getId(),
                world.getName(),
                world.getDatacenter().getId(),
                world.getDatacenter().getName(),
                counts.small(),
                counts.medium(),
                counts.large(),
                counts.total());
    }
}
