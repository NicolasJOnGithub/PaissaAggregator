package com.paissa.aggregator.query;

import com.paissa.aggregator.housing.Plot;
import com.paissa.aggregator.housing.PlotSize;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@code districtIds} params are always the full set of known district ids when the caller means
 * "no district filter" (see {@code QueryService}) rather than null — a null/empty collection bound
 * to an {@code IN} clause is a known Hibernate footgun, so callers normalize away the "no filter"
 * case before it gets here instead of this repository trying to special-case it in JPQL.
 */
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
              and wd.district.id in :districtIds
            group by w.id, w.name, dc.id, dc.name, p.size
            """)
    List<WorldSizeCountRow> countByWorldAndSize(
            @Param("purchaseSystemCodes") List<Integer> purchaseSystemCodes,
            @Param("datacenterId") Integer datacenterId,
            @Param("districtIds") List<Integer> districtIds);

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
              and p.size in :sizes
              and p.purchaseSystem in :purchaseSystemCodes
              and d.id in :districtIds
            order by d.id asc, wd.wardNumber asc, p.plotNumber asc
            """)
    Page<Plot> findWorldPlots(
            @Param("worldId") Integer worldId,
            @Param("sizes") List<PlotSize> sizes,
            @Param("purchaseSystemCodes") List<Integer> purchaseSystemCodes,
            @Param("districtIds") List<Integer> districtIds,
            Pageable pageable);
}
