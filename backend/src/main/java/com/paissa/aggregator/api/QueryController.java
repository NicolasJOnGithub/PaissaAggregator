package com.paissa.aggregator.api;

import com.paissa.aggregator.housing.PlotSize;
import com.paissa.aggregator.housing.PurchaseSystem;
import com.paissa.aggregator.query.DatacenterSummary;
import com.paissa.aggregator.query.QueryService;
import com.paissa.aggregator.query.WorldStats;
import com.paissa.aggregator.world.Region;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    @GetMapping("/api/datacenters")
    public List<DatacenterSummary> datacenters(@RequestParam(required = false) Region region) {
        return queryService.datacenterSummaries(region);
    }

    @GetMapping("/api/leaderboard/worlds")
    public List<WorldStats> worldLeaderboard(
            @RequestParam(required = false) List<PlotSize> size,
            @RequestParam(required = false) PurchaseSystem ownership,
            @RequestParam(required = false) Integer datacenterId,
            @RequestParam(required = false) List<Integer> districtId,
            @RequestParam(required = false) Region region) {
        return queryService.worldLeaderboard(size, ownership, datacenterId, districtId, region);
    }
}
