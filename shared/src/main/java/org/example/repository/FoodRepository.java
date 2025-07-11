package org.example.repository;

import org.example.model.Food;
import org.example.model.Athlete;
import org.example.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    
    List<Food> findByMealDayAthleteOrderByMealDayDateDesc(Athlete athlete);
    
    List<Food> findByMealOrderByIdAsc(Meal meal);
    
}
