package org.example.repository;

import org.example.model.Meal;
import org.example.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    
    List<Meal> findByDayAthleteOrderByDayDateDesc(Athlete athlete);
    
}
