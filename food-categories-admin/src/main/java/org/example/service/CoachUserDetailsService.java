package org.example.service;

import org.example.model.Coach;
import org.example.repository.CoachRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CoachUserDetailsService implements UserDetailsService {

    private final CoachRepository coachRepository;

    public CoachUserDetailsService(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Coach coach = coachRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Coach not found: " + username));

        return new User(
                coach.getUsername(),
                coach.getPassword(),
                Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_COACH")
                )
        );
    }
} 