package org.example.service;

import org.example.model.Coach;
import org.example.repository.CoachRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CoachUserDetailsService implements UserDetailsService {

    private final CoachRepository coachRepo;

    public CoachUserDetailsService(CoachRepository coachRepo) {
        this.coachRepo = coachRepo;
    }

    /**
     * Called by Spring Security to load a UserDetails object (here, our Coach)
     * during authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return coachRepo.findByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("No coach found with username: " + username)
                );
    }

    /**
     * For your controllers and services: lookup the full Coach entity
     * by username (so you can access things like coach.getAthletes()).
     */
    public Coach loadCoachByUsername(String username) {
        return coachRepo.findByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("No coach found with username: " + username)
                );
    }

}
