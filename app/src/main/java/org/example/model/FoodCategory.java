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
    private Float prot;

    @Column(nullable = true)
    private Float kcal;

    @Column(nullable = true)
    private Float fat;

    @Column(nullable = true)
    private Float carb;

    // NOTE: Removed foods relationship since Food and FoodCategory are in different databases

    // --- Constructors ---
    public FoodCategory() { }

    public FoodCategory(String name) {
        this.name = name;
    }

    public FoodCategory(String name, Float prot, Float kcal, Float fat, Float carb) {
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

    public Float getProt() {
        return prot;
    }

    public void setProt(Float prot) {
        this.prot = prot;
    }

    public Float getKcal() {
        return kcal;
    }

    public void setKcal(Float kcal) {
        this.kcal = kcal;
    }

    public Float getFat() {
        return fat;
    }

    public void setFat(Float fat) {
        this.fat = fat;
    }

    public Float getCarb() {
        return carb;
    }

    public void setCarb(Float carb) {
        this.carb = carb;
    }

    // NOTE: Removed foods relationship methods since Food and FoodCategory are in different databases
}
