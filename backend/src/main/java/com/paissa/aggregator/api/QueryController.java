package com.paissa.aggregator.api;

import com.paissa.aggregator.housing.PlotSize;
import com.paissa.aggregator.housing.PurchaseSystem;
import com.paissa.aggregator.query.DatacenterSummary;
import com.paissa.aggregator.query.QueryService;
import com.paissa.aggregator.query.WorldStats;
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
    public List<DatacenterSummary> datacenters() {
        return queryService.datacenterSummaries();
    }

    @GetMapping("/api/leaderboard/worlds")
    public List<WorldStats> worldLeaderboard(
            @RequestParam(required = false) PlotSize size,
            @RequestParam(required = false) PurchaseSystem ownership,
            @RequestParam(required = false) Integer datacenterId,
            @RequestParam(required = false) Integer districtId) {
        return queryService.worldLeaderboard(size, ownership, datacenterId, districtId);
    }
}
