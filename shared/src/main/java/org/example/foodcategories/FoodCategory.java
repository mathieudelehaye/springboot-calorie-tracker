package org.example.foodcategories;

import jakarta.persistence.*;

@Entity
@Table(name = "food_categories")
public class FoodCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "prot")
    private Double prot;

    @Column(name = "carb")
    private Double carb;

    @Column(name = "fat")
    private Double fat;

    @Column(name = "kcal")
    private Double kcal;

    // Constructors
    public FoodCategory() {}

    public FoodCategory(String name, Double prot, Double carb, Double fat, Double kcal) {
        this.name = name;
        this.prot = prot;
        this.carb = carb;
        this.fat = fat;
        this.kcal = kcal;
    }

    // Getters and Setters
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

    public Double getProt() {
        return prot;
    }

    public void setProt(Double prot) {
        this.prot = prot;
    }

    public Double getCarb() {
        return carb;
    }

    public void setCarb(Double carb) {
        this.carb = carb;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getKcal() {
        return kcal;
    }

    public void setKcal(Double kcal) {
        this.kcal = kcal;
    }

    @Override
    public String toString() {
        return "FoodCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prot=" + prot +
                ", carb=" + carb +
                ", fat=" + fat +
                ", kcal=" + kcal +
                '}';
    }
} 