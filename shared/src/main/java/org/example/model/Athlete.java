package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "athletes")
public class Athlete extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private double size;

    @Column(nullable = true)  // Make email optional
    private String email;

    @Column(nullable = true)  // Make phone optional
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Day> days = new ArrayList<>();

    public Athlete() {}

    public Athlete(String name, double weight, double size, Coach coach) {
        this.name   = name;
        this.weight = weight;
        this.size   = size;
        this.coach  = coach;
    }

    // --- Getters & setters ---

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getSize() {
        return size;
    }
    public void setSize(double size) {
        this.size = size;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Coach getCoach() {
        return coach;
    }
    public void setCoach(Coach coach) {
        this.coach = coach;
        if (coach != null && !coach.getAthletes().contains(this)) {
            coach.getAthletes().add(this);
        }
    }

    public List<Day> getDays() {
        return days;
    }
    public void setDays(List<Day> days) {
        this.days = days;
    }

    // --- Relationship helper methods ---

    public void addDay(Day day) {
        if (!days.contains(day)) {
            days.add(day);
        }
        if (day.getAthlete() != this) {
            day.setAthlete(this);
        }
    }

    public void removeDay(Day day) {
        days.remove(day);
        if (day.getAthlete() == this) {
            day.setAthlete(null);
        }
    }
}
