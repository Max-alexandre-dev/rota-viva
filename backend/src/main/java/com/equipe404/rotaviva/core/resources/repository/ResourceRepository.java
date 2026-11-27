package com.equipe404.rotaviva.core.resources.repository;

import com.equipe404.rotaviva.core.resources.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
}
