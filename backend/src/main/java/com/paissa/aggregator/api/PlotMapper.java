package com.paissa.aggregator.api;

import com.paissa.aggregator.api.dto.PlotDto;
import com.paissa.aggregator.housing.Plot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps the persisted (0-indexed) Plot entity to its API DTO, applying the +1 in-game display offset. */
@Mapper(componentModel = "spring")
public interface PlotMapper {

    @Mapping(target = "worldId", source = "ward.world.id")
    @Mapping(target = "worldName", source = "ward.world.name")
    @Mapping(target = "districtId", source = "ward.district.id")
    @Mapping(target = "districtName", source = "ward.district.name")
    @Mapping(target = "wardNumber", expression = "java(plot.getWard().getWardNumber() + 1)")
    @Mapping(target = "plotNumber", expression = "java(plot.getPlotNumber() + 1)")
    @Mapping(target = "size", expression = "java(plot.getSize().name())")
    @Mapping(
            target = "ownership",
            expression =
                    "java(com.paissa.aggregator.housing.PurchaseSystem.fromRawCode(plot.getPurchaseSystem()).name())")
    PlotDto toDto(Plot plot);
}
