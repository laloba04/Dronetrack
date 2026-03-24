package com.skyfence.repository;

import com.skyfence.model.RestrictedZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestrictedZoneRepository extends JpaRepository<RestrictedZone, Long> {
}
