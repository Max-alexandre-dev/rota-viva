package com.equipe404.rotaviva.core.coordinators.service;

import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorRequest;
import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorResponse;
import com.equipe404.rotaviva.core.coordinators.entity.Coordinator;
import com.equipe404.rotaviva.core.coordinators.exception.CoordinatorEmailAlreadyExistsException;
import com.equipe404.rotaviva.core.coordinators.exception.CoordinatorNotFoundException;
import com.equipe404.rotaviva.core.coordinators.mapper.CoordinatorMapper;
import com.equipe404.rotaviva.core.coordinators.repository.CoordinatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinatorService {

    private final CoordinatorRepository repository;
    private final CoordinatorMapper mapper;

    @Transactional
    public CoordinatorResponse create(CoordinatorRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new CoordinatorEmailAlreadyExistsException();
        }
        return mapper.toResponse(saveAndFlush(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<CoordinatorResponse> list() {
        return repository.findAll(Sort.by("id")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CoordinatorResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public CoordinatorResponse update(Long id, CoordinatorRequest request) {
        Coordinator coordinator = find(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new CoordinatorEmailAlreadyExistsException();
        }
        mapper.updateEntity(request, coordinator);
        return mapper.toResponse(saveAndFlush(coordinator));
    }

    private Coordinator find(Long id) {
        return repository.findById(id).orElseThrow(() -> new CoordinatorNotFoundException(id));
    }

    private Coordinator saveAndFlush(Coordinator coordinator) {
        try {
            return repository.saveAndFlush(coordinator);
        } catch (DataIntegrityViolationException exception) {
            throw new CoordinatorEmailAlreadyExistsException();
        }
    }
}
