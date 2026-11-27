package com.equipe404.rotaviva.core.resources.mapper;

import com.equipe404.rotaviva.core.resources.dto.ResourceRequest;
import com.equipe404.rotaviva.core.resources.dto.ResourceResponse;
import com.equipe404.rotaviva.core.resources.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResourceMapper {

    ResourceResponse toResponse(Resource resource);

    @Mapping(target = "id", ignore = true)
    Resource toEntity(ResourceRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ResourceRequest request, @MappingTarget Resource resource);
}
