package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_categories")
public class FoodCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    // One category has many foods
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();

    // --- Constructors ---
    public FoodCategory() { }

    public FoodCategory(String name) {
        this.name = name;
    }

    // --- Getters & setters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    // --- Relationship helper methods ---
    public void addFood(Food food) {
        foods.add(food);
        if (food.getCategory() != this) {
            food.setCategory(this);
        }
    }

    public void removeFood(Food food) {
        foods.remove(food);
        if (food.getCategory() == this) {
            food.setCategory(null);
        }
    }
}
