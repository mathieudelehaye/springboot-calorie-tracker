package org.example.repository;

import org.example.model.Day;
import org.example.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayRepository extends JpaRepository<Day, Long> {
    
    List<Day> findByAthleteOrderByDateDesc(Athlete athlete);
    
}
