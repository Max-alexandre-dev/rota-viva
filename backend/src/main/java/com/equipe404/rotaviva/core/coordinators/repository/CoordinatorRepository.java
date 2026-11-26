package com.equipe404.rotaviva.core.coordinators.repository;

import com.equipe404.rotaviva.core.coordinators.entity.Coordinator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
