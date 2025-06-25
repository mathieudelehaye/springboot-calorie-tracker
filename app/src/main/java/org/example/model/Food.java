package org.example.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "foods")
public class Food extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FoodCategory category;

    public Food() { }

    public Food(String name, Meal meal, FoodCategory category) {
        this.name = name;
        this.quantity = 1;
        setMeal(meal);
        setCategory(category);
    }

    public Food(String name, int quantity, Meal meal, FoodCategory category) {
        this.name = name;
        this.quantity = quantity;
        setMeal(meal);
        setCategory(category);
    }

    // --- Getters & Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Meal getMeal() { return meal; }
    public void setMeal(Meal meal) {
        this.meal = meal;
        if (meal != null && !meal.getFoods().contains(this)) {
            meal.addFood(this);
        }
    }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) {
        this.category = category;
        if (category != null && !category.getFoods().contains(this)) {
            category.getFoods().add(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Food)) return false;
        Food food = (Food) o;
        return name.equals(food.name) &&
               Objects.equals(meal, food.meal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, meal);
    }
}
