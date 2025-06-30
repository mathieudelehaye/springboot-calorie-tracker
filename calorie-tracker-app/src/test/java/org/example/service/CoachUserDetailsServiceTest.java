package org.example.service;

import org.example.model.Coach;
import org.example.repository.CoachRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoachUserDetailsServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CoachUserDetailsService userDetailsService;

    private Coach testCoach;

    @BeforeEach
    void setUp() {
        testCoach = new Coach();
        testCoach.setId(1L);
        testCoach.setUsername("testcoach");
        testCoach.setPassword("encodedPassword");
        testCoach.setRole(org.example.model.Role.ROLE_COACH);
    }

    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Given
        String username = "testcoach";
        when(coachRepository.findByUsername(username)).thenReturn(Optional.of(testCoach));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COACH")));
        verify(coachRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(coachRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });
        verify(coachRepository).findByUsername(username);
    }

    @Test
    void loadCoachByUsername_WithValidUsername_ShouldReturnCoach() {
        // Given
        String username = "testcoach";
        when(coachRepository.findByUsername(username)).thenReturn(Optional.of(testCoach));

        // When
        Coach result = userDetailsService.loadCoachByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(testCoach.getId(), result.getId());
        assertEquals(testCoach.getUsername(), result.getUsername());
        verify(coachRepository).findByUsername(username);
    }

    @Test
    void loadCoachByUsername_WithInvalidUsername_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(coachRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadCoachByUsername(username);
        });
        verify(coachRepository).findByUsername(username);
    }
} 