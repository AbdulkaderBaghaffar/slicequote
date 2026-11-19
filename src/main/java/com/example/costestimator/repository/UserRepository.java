package com.example.costestimator.repository;

import com.example.costestimator.data.User;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Service
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailIgnoreCase(String email);
}
