package com.example.costestimator.repository;

import com.example.costestimator.data.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UploadRepository extends JpaRepository<Upload, Long> {
  List<Upload> findByOwnerId(Long ownerId);

  Optional<Upload> findByIdandOwnerId(Long id, Long ownerId);
}
