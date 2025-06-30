package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AthleteTest {

    private Athlete athlete;
    private Coach coach;
    private Day day;

    @BeforeEach
    void setUp() {
        coach = new Coach();
        coach.setId(1L);
        coach.setUsername("testcoach");

        athlete = new Athlete("John Doe", 70.0, 180.0, coach);
        athlete.setId(1L);

        day = new Day();
        day.setId(1L);
        day.setDayName("Monday");
    }

    @Test
    void constructor_WithValidParameters_ShouldCreateAthlete() {
        // When
        Athlete newAthlete = new Athlete("Jane Smith", 65.0, 170.0, coach);

        // Then
        assertEquals("Jane Smith", newAthlete.getName());
        assertEquals(65.0, newAthlete.getWeight());
        assertEquals(170.0, newAthlete.getSize());
        assertEquals(coach, newAthlete.getCoach());
        assertTrue(newAthlete.getDays().isEmpty());
    }

    @Test
    void setCoach_ShouldEstablishBidirectionalRelationship() {
        // Given
        Athlete newAthlete = new Athlete();
        
        // When
        newAthlete.setCoach(coach);

        // Then
        assertEquals(coach, newAthlete.getCoach());
        assertTrue(coach.getAthletes().contains(newAthlete));
    }

    @Test
    void addDay_ShouldAddDayAndEstablishRelationship() {
        // When
        athlete.addDay(day);

        // Then
        assertTrue(athlete.getDays().contains(day));
        assertEquals(athlete, day.getAthlete());
    }

    @Test
    void addDay_WhenDayAlreadyHasAthlete_ShouldNotDuplicateRelationship() {
        // Given
        day.setAthlete(athlete);
        
        // When
        athlete.addDay(day);

        // Then
        assertTrue(athlete.getDays().contains(day));
        assertEquals(athlete, day.getAthlete());
        assertEquals(1, athlete.getDays().size());
    }

    @Test
    void removeDay_ShouldRemoveDayAndClearRelationship() {
        // Given
        athlete.addDay(day);
        
        // When
        athlete.removeDay(day);

        // Then
        assertFalse(athlete.getDays().contains(day));
        assertNull(day.getAthlete());
    }

    @Test
    void removeDay_WhenDayNotAssociatedWithAthlete_ShouldNotAffectOtherDay() {
        // Given
        Day otherDay = new Day();
        otherDay.setDayName("Tuesday");
        otherDay.setAthlete(athlete);
        athlete.addDay(otherDay);
        
        // When
        athlete.removeDay(day); // day is not associated with athlete

        // Then
        assertTrue(athlete.getDays().contains(otherDay));
        assertEquals(athlete, otherDay.getAthlete());
    }

    @Test
    void setEmail_ShouldUpdateEmailField() {
        // When
        athlete.setEmail("john.doe@example.com");

        // Then
        assertEquals("john.doe@example.com", athlete.getEmail());
    }

    @Test
    void setPhone_ShouldUpdatePhoneField() {
        // When
        athlete.setPhone("+1234567890");

        // Then
        assertEquals("+1234567890", athlete.getPhone());
    }

    @Test
    void setWeight_ShouldUpdateWeightField() {
        // When
        athlete.setWeight(75.0);

        // Then
        assertEquals(75.0, athlete.getWeight());
    }

    @Test
    void setSize_ShouldUpdateSizeField() {
        // When
        athlete.setSize(185.0);

        // Then
        assertEquals(185.0, athlete.getSize());
    }

    @Test
    void setName_ShouldUpdateNameField() {
        // When
        athlete.setName("John Smith");

        // Then
        assertEquals("John Smith", athlete.getName());
    }
} 