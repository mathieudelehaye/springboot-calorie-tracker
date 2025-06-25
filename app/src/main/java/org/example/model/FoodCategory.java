package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_categories")
public class FoodCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = true)
    private float prot;

    @Column(nullable = true)
    private float kcal;

    @Column(nullable = true)
    private float fat;

    @Column(nullable = true)
    private float carb;

    // One category has many foods
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();

    // --- Constructors ---
    public FoodCategory() { }

    public FoodCategory(String name) {
        this.name = name;
    }

    public FoodCategory(String name, float prot, float kcal, float fat, float carb) {
        this.name = name;
        this.prot = prot;
        this.kcal = kcal;
        this.fat = fat;
        this.carb = carb;
    }

    // --- Getters & setters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getProt() {
        return prot;
    }

    public void setProt(float prot) {
        this.prot = prot;
    }

    public float getKcal() {
        return kcal;
    }

    public void setKcal(float kcal) {
        this.kcal = kcal;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getCarb() {
        return carb;
    }

    public void setCarb(float carb) {
        this.carb = carb;
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
