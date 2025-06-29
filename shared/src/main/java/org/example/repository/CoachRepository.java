package org.example.repository;

import org.example.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByUsername(String username);
}
