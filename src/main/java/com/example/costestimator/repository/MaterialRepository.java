package com.example.costestimator.repository;

import com.example.costestimator.data.Material;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
  List<Material> findByActiveTrue();
}
