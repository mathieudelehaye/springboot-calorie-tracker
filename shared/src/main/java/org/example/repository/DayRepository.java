package org.example.repository;

import org.example.model.Day;
import org.example.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DayRepository extends JpaRepository<Day, Long> {
    
    List<Day> findByAthleteOrderByDateDesc(Athlete athlete);
    
    Optional<Day> findByAthleteAndDayName(Athlete athlete, String dayName);
    
    Optional<Day> findByAthleteAndDayNameAndIdNot(Athlete athlete, String dayName, Long id);
    
}
