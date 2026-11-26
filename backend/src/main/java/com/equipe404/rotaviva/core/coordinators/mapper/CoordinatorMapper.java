package com.equipe404.rotaviva.core.coordinators.mapper;

import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorRequest;
import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorResponse;
import com.equipe404.rotaviva.core.coordinators.entity.Coordinator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CoordinatorMapper {

    CoordinatorResponse toResponse(Coordinator coordinator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Coordinator toEntity(CoordinatorRequest request);

    void updateEntity(CoordinatorRequest request, @MappingTarget Coordinator coordinator);
}
