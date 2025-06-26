package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meals")
public class Meal extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();

    // Constructors

    public Meal() { }

    public Meal(String name, Day day) {
        this.name = name;
        setDay(day);
    }

    // Getters & Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
        if (day != null && !day.getMeals().contains(this)) {
            day.addMeal(this);
        }
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    // Relationship helpers

    public void addFood(Food food) {
        foods.add(food);
        if (food.getMeal() != this) {
            food.setMeal(this);
        }
    }

    public void removeFood(Food food) {
        foods.remove(food);
        if (food.getMeal() == this) {
            food.setMeal(null);
        }
    }

    // Calculate total kcal for this meal
    @Transient
    public float calculateTotalKcal() {
        return (float) foods.stream()
                    .mapToDouble(food -> {
                        Float categoryKcal = food.getCategory().getKcal();
                        return (categoryKcal != null ? categoryKcal : 0.0f) * food.getQuantity() / 100.0f;
                    })
                    .sum();
    }

    // Calculate total protein for this meal
    @Transient
    public float calculateTotalProtein() {
        return (float) foods.stream()
                    .mapToDouble(food -> {
                        Float categoryProt = food.getCategory().getProt();
                        return (categoryProt != null ? categoryProt : 0.0f) * food.getQuantity() / 100.0f;
                    })
                    .sum();
    }

    // Calculate total carbs for this meal
    @Transient
    public float calculateTotalCarbs() {
        return (float) foods.stream()
                    .mapToDouble(food -> {
                        Float categoryCarb = food.getCategory().getCarb();
                        return (categoryCarb != null ? categoryCarb : 0.0f) * food.getQuantity() / 100.0f;
                    })
                    .sum();
    }

    // Calculate total fat for this meal
    @Transient
    public float calculateTotalFat() {
        return (float) foods.stream()
                    .mapToDouble(food -> {
                        Float categoryFat = food.getCategory().getFat();
                        return (categoryFat != null ? categoryFat : 0.0f) * food.getQuantity() / 100.0f;
                    })
                    .sum();
    }
}
