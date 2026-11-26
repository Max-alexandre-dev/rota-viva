package com.equipe404.rotaviva.core.centers.mapper;

import com.equipe404.rotaviva.core.centers.dto.CenterRequest;
import com.equipe404.rotaviva.core.centers.dto.CenterResponse;
import com.equipe404.rotaviva.core.centers.entity.Center;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CenterMapper {

    CenterResponse toResponse(Center center);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Center toEntity(CenterRequest request);

    void updateEntity(CenterRequest request, @MappingTarget Center center);
}
