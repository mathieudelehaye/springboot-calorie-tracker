package org.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "athletes")
public class Athlete extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Day> days = new ArrayList<>();

    public Athlete() {}

    public Athlete(String name, String email, String phone, Coach coach) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.coach = coach;
    }

    // --- Getters & setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) {
        this.coach = coach;
        if (coach != null && !coach.getAthletes().contains(this)) {
            coach.addAthlete(this);
        }
    }

    public List<Day> getDays() { return days; }
    public void setDays(List<Day> days) { this.days = days; }

    // --- Relationship helper methods ---

    public void addDay(Day day) {
        days.add(day);
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
