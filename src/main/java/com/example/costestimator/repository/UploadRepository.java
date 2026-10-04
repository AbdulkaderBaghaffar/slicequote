package com.example.costestimator.repository;

import com.example.costestimator.data.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadRepository extends JpaRepository<Upload, Long> {
  List<Upload> findByOwnerId(Long ownerId);

}
