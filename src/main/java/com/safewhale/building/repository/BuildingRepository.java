package com.safewhale.building.repository;

import com.safewhale.building.domain.Building;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByNameContainingIgnoreCaseOrderByName(String name);
}
