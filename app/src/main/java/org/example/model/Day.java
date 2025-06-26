package org.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "days")
public class Day extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = true)
    private String dayName; // Monday, Tuesday, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals = new ArrayList<>();

    // Constructors

    public Day() { }

    public Day(LocalDate date, Athlete athlete) {
        this.date = date;
        setAthlete(athlete);
    }

    public Day(LocalDate date, String dayName, Athlete athlete) {
        this.date = date;
        this.dayName = dayName;
        setAthlete(athlete);
    }

    // Getters and Setters

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
        if (athlete != null && !athlete.getDays().contains(this)) {
            athlete.addDay(this);
        }
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    // Relationship helper methods

    public void addMeal(Meal meal) {
        meals.add(meal);
        if (meal.getDay() != this) {
            meal.setDay(this);
        }
    }

    public void removeMeal(Meal meal) {
        meals.remove(meal);
        if (meal.getDay() == this) {
            meal.setDay(null);
        }
    }

    // Optional: calculate total kcal for the day
    @Transient
    public float calculateTotalKcal() {
        return (float) meals.stream()
                    .mapToDouble(meal -> meal.calculateTotalKcal())
                    .sum();
    }

    // Calculate total protein for the day
    @Transient
    public float calculateTotalProtein() {
        return (float) meals.stream()
                    .mapToDouble(meal -> meal.calculateTotalProtein())
                    .sum();
    }

    // Calculate total carbs for the day
    @Transient
    public float calculateTotalCarbs() {
        return (float) meals.stream()
                    .mapToDouble(meal -> meal.calculateTotalCarbs())
                    .sum();
    }

    // Calculate total fat for the day
    @Transient
    public float calculateTotalFat() {
        return (float) meals.stream()
                    .mapToDouble(meal -> meal.calculateTotalFat())
                    .sum();
    }
}
