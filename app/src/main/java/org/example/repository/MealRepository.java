package org.example.repository;

import org.example.model.Meal;
import org.example.model.Athlete;
import org.example.model.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {
    
    List<Meal> findByDayAthleteOrderByDayDateDesc(Athlete athlete);
    
    List<Meal> findByDayOrderByIdAsc(Day day);
    
    Optional<Meal> findByDayAndName(Day day, String name);
    
    Optional<Meal> findByDayAndNameAndIdNot(Day day, String name, Long id);
    
}
