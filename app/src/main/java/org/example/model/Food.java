package org.example.model;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Objects;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    public Food() { }

    public Food(Meal meal, Long categoryId) {
        this.quantity = 1;
        setMeal(meal);
        setCategoryId(categoryId);
    }

    public Food(int quantity, Meal meal, Long categoryId) {
        this.quantity = quantity;
        setMeal(meal);
        setCategoryId(categoryId);
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Meal getMeal() { return meal; }
    public void setMeal(Meal meal) {
        this.meal = meal;
        if (meal != null && !meal.getFoods().contains(this)) {
            meal.addFood(this);
        }
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Food)) return false;
        Food food = (Food) o;
        return Objects.equals(categoryId, food.categoryId) &&
               Objects.equals(meal, food.meal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, meal);
    }
}
