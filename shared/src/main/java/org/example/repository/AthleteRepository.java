package org.example.repository;

import org.example.model.Athlete;
import org.example.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * JPA repository for Athlete entities.
 * 
 * We add:
 *  • findByCoach(Coach)         – to look up by the coach entity  
 *  • findByCoachUsername(String) – to look up by the coach’s username  
 */
public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    /**
     * Fetch all athletes belonging to the given Coach.
     */
    List<Athlete> findByCoach(Coach coach);

    /**
     * Fetch all athletes for the coach with the given username.
     */
    List<Athlete> findByCoachUsername(String username);
}
