package com.equipe404.rotaviva.core.resources.service;

import com.equipe404.rotaviva.core.resources.dto.ResourceRequest;
import com.equipe404.rotaviva.core.resources.dto.ResourceResponse;
import com.equipe404.rotaviva.core.resources.entity.Resource;
import com.equipe404.rotaviva.core.resources.exception.ResourceNotFoundException;
import com.equipe404.rotaviva.core.resources.mapper.ResourceMapper;
import com.equipe404.rotaviva.core.resources.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final Sort LIST_ORDER = Sort.by("name", "id");

    private final ResourceRepository repository;
    private final ResourceMapper mapper;

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        return mapper.toResponse(repository.saveAndFlush(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> list() {
        return repository.findAll(LIST_ORDER).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public ResourceResponse update(UUID id, ResourceRequest request) {
        Resource resource = find(id);
        mapper.updateEntity(request, resource);
        return mapper.toResponse(repository.saveAndFlush(resource));
    }

    private Resource find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }
}
