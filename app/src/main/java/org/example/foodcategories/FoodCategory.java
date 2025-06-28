package org.example.foodcategories;

import jakarta.persistence.*;

@Entity
@Table(name = "food_categories")
public class FoodCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodCategory that = (FoodCategory) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 