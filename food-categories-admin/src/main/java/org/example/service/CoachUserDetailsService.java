package org.example.service;

import org.example.model.Coach;
import org.example.repository.CoachRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import org.springframework.dao.DataAccessException;

@Service
public class CoachUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CoachUserDetailsService.class);
    private final CoachRepository coachRepository;

    public CoachUserDetailsService(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional(readOnly = true, value = "transactionManager")
    @Retryable(
        value = {DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            logger.debug("Attempting to load coach with username: {}", username);
            Coach coach = coachRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Coach not found: " + username));

            logger.debug("Successfully loaded coach: {}", username);
            return new User(
                    coach.getUsername(),
                    coach.getPassword(),
                    Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_COACH")
                    )
            );
        } catch (DataAccessException e) {
            logger.error("Database error while loading user {}: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Database error occurred", e);
        } catch (Exception e) {
            logger.error("Unexpected error while loading user {}: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("System error occurred", e);
        }
    }
} 