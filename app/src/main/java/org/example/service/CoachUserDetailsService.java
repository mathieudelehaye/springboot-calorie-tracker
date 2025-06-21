package org.example.service;

import org.example.model.Coach;
import org.example.repository.CoachRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CoachUserDetailsService implements UserDetailsService {

    private final CoachRepository coachRepo;

    public CoachUserDetailsService(CoachRepository coachRepo) {
        this.coachRepo = coachRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return coachRepo.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("Coach not found with username: " + username)
            );
    }
}
