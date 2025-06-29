package org.example.model;

import jakarta.persistence.*;
import java.util.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "coaches")
public class Coach extends BaseEntity implements UserDetails, CredentialsContainer {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // will be hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Athlete> athletes = new ArrayList<>();

    // Getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Athlete> getAthletes() { return athletes; }

    // Helper methods to keep relationships aligned
    public void addAthlete(Athlete a) {
        athletes.add(a);
        a.setCoach(this);
    }

    public void removeAthlete(Athlete a) {
        athletes.remove(a);
        a.setCoach(null);
    }

    // --- UserDetails implementation ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    // --- CredentialsContainer implementation ---
    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
