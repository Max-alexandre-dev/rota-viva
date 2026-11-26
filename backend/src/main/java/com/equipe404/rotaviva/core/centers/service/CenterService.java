package com.equipe404.rotaviva.core.centers.service;

import com.equipe404.rotaviva.core.centers.dto.CenterRequest;
import com.equipe404.rotaviva.core.centers.dto.CenterResponse;
import com.equipe404.rotaviva.core.centers.entity.Center;
import com.equipe404.rotaviva.core.centers.exception.CenterNotFoundException;
import com.equipe404.rotaviva.core.centers.mapper.CenterMapper;
import com.equipe404.rotaviva.core.centers.repository.CenterRepository;
import com.equipe404.rotaviva.core.coordinators.entity.Coordinator;
import com.equipe404.rotaviva.core.coordinators.exception.CoordinatorNotFoundException;
import com.equipe404.rotaviva.core.coordinators.repository.CoordinatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CenterService {

    private static final Sort LIST_ORDER = Sort.by("name", "id");

    private final CenterRepository repository;
    private final CenterMapper mapper;
    private final CoordinatorRepository coordinatorRepository;

    @Transactional
    public CenterResponse create(CenterRequest request) {
        Center center = mapper.toEntity(request);
        center.setCoordinator(findCoordinator(request.coordinatorId()));
        return mapper.toResponse(repository.saveAndFlush(center));
    }

    @Transactional(readOnly = true)
    public List<CenterResponse> list() {
        return repository.findAll(LIST_ORDER).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CenterResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public CenterResponse update(UUID id, CenterRequest request) {
        Center center = find(id);
        mapper.updateEntity(request, center);
        center.setCoordinator(findCoordinator(request.coordinatorId()));
        return mapper.toResponse(repository.saveAndFlush(center));
    }

    private Center find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new CenterNotFoundException(id));
    }

    private Coordinator findCoordinator(Long coordinatorId) {
        return coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> new CoordinatorNotFoundException(coordinatorId));
    }
}
