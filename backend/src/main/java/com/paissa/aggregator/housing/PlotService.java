package com.paissa.aggregator.housing;

import com.paissa.aggregator.world.District;
import com.paissa.aggregator.world.World;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlotService {

    private final WardRepository wardRepository;
    private final PlotRepository plotRepository;

    /**
     * Wipes all wards/plots for this world and re-inserts them from freshly fetched data.
     * Idempotent: safe to call repeatedly with the latest PAISSADB snapshot for the world.
     */
    @Transactional
    public void replaceWorldHousingData(World world, List<DistrictOpenPlots> districtOpenPlots) {
        wardRepository.deleteAllByWorldId(world.getId());
        wardRepository.flush();

        for (DistrictOpenPlots districtPlots : districtOpenPlots) {
            Map<Integer, List<PlotInput>> plotsByWard = new LinkedHashMap<>();
            for (PlotInput plot : districtPlots.plots()) {
                plotsByWard.computeIfAbsent(plot.wardNumber(), k -> new ArrayList<>()).add(plot);
            }

            for (Map.Entry<Integer, List<PlotInput>> entry : plotsByWard.entrySet()) {
                Ward ward = new Ward();
                ward.setWorld(world);
                ward.setDistrict(districtPlots.district());
                ward.setWardNumber(entry.getKey());
                ward = wardRepository.save(ward);

                List<Plot> plots = new ArrayList<>();
                for (PlotInput input : entry.getValue()) {
                    Plot plot = new Plot();
                    plot.setWard(ward);
                    plot.setPlotNumber(input.plotNumber());
                    plot.setSize(PlotSize.fromCode(input.sizeCode()));
                    plot.setPrice(input.price());
                    plot.setPurchaseSystem(input.purchaseSystemRawCode());
                    plot.setLottoEntries(input.lottoEntries());
                    plot.setLottoPhase(input.lottoPhase());
                    plot.setLottoPhaseUntil(input.lottoPhaseUntil());
                    plot.setFirstSeenTime(input.firstSeenTime());
                    plot.setLastUpdatedTime(input.lastUpdatedTime());
                    plots.add(plot);
                }
                plotRepository.saveAll(plots);
            }
        }
    }

    public record DistrictOpenPlots(District district, List<PlotInput> plots) {}

    public record PlotInput(
            Integer wardNumber,
            Integer plotNumber,
            Integer sizeCode,
            Long price,
            Integer purchaseSystemRawCode,
            Integer lottoEntries,
            Integer lottoPhase,
            Long lottoPhaseUntil,
            Double firstSeenTime,
            Double lastUpdatedTime) {}
}
