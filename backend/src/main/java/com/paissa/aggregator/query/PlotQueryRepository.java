package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.Plot;
import com.paissa.aggregator.housing.PlotSize;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlotQueryRepository extends JpaRepository<Plot, Long> {

    @Query(
            """
            select new com.paissa.aggregator.query.DatacenterSizeCountRow(dc.id, dc.name, p.size, count(p.id))
            from Plot p join p.ward wd join wd.world w join w.datacenter dc
            group by dc.id, dc.name, p.size
            """)
    List<DatacenterSizeCountRow> countAllByDatacenterAndSize();

    @Query(
            """
            select new com.paissa.aggregator.query.WorldSizeCountRow(w.id, w.name, dc.id, dc.name, p.size, count(p.id))
            from Plot p join p.ward wd join wd.world w join w.datacenter dc
            where p.purchaseSystem in :purchaseSystemCodes
              and (:datacenterId is null or dc.id = :datacenterId)
            group by w.id, w.name, dc.id, dc.name, p.size
            """)
    List<WorldSizeCountRow> countByWorldAndSize(
            @Param("purchaseSystemCodes") List<Integer> purchaseSystemCodes, @Param("datacenterId") Integer datacenterId);

    @Query(
            """
            select new com.paissa.aggregator.query.WorldSizeCountRow(w.id, w.name, dc.id, dc.name, p.size, count(p.id))
            from Plot p join p.ward wd join wd.world w join w.datacenter dc
            where w.id = :worldId
            group by w.id, w.name, dc.id, dc.name, p.size
            """)
    List<WorldSizeCountRow> countByWorldIdAndSize(@Param("worldId") Integer worldId);

    @Query(
            """
            select p from Plot p
            join fetch p.ward wd
            join fetch wd.district d
            join fetch wd.world w
            where w.id = :worldId
              and p.purchaseSystem in :purchaseSystemCodes
            order by d.id asc, wd.wardNumber asc, p.plotNumber asc
            """)
    Page<Plot> findWorldPlots(
            @Param("worldId") Integer worldId, @Param("purchaseSystemCodes") List<Integer> purchaseSystemCodes, Pageable pageable);

    @Query(
            """
            select p from Plot p
            join fetch p.ward wd
            join fetch wd.district d
            join fetch wd.world w
            where w.id = :worldId
              and p.size = :size
              and p.purchaseSystem in :purchaseSystemCodes
            order by d.id asc, wd.wardNumber asc, p.plotNumber asc
            """)
    Page<Plot> findWorldPlotsBySize(
            @Param("worldId") Integer worldId,
            @Param("size") PlotSize size,
            @Param("purchaseSystemCodes") List<Integer> purchaseSystemCodes,
            Pageable pageable);
}
