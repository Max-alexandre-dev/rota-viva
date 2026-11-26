package com.equipe404.rotaviva.core.centers.repository;

import com.equipe404.rotaviva.core.centers.entity.Center;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CenterRepository extends JpaRepository<Center, UUID> {
}
