package org.example.controller;

import org.example.foodcategories.FoodCategory;
import org.example.model.*;
import org.example.repository.*;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.example.service.CoachUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private AthleteRepository athleteRepo;
    
    @Mock
    private DayRepository dayRepo;
    
    @Mock
    private MealRepository mealRepo;
    
    @Mock
    private FoodRepository foodRepo;
    
    @Mock
    private FoodCategoryRepository foodCategoryRepo;
    
    @Mock
    private CoachUserDetailsService coachService;
    
    @Mock
    private Model model;
    
    @Mock
    private Principal principal;

    @InjectMocks
    private DashboardController controller;

    private Coach testCoach;
    private Athlete testAthlete;
    private Day testDay;
    private Meal testMeal;
    private Food testFood;
    private FoodCategory testFoodCategory;

    @BeforeEach
    void setUp() {
        // Create test entities
        testCoach = new Coach();
        testCoach.setId(1L);
        testCoach.setUsername("testcoach");

        testAthlete = new Athlete("John Doe", 70.0, 180.0, testCoach);
        testAthlete.setId(1L);

        testDay = new Day();
        testDay.setId(1L);
        testDay.setDayName("Monday");
        testDay.setAthlete(testAthlete);
        testDay.setDate(LocalDate.now());

        testMeal = new Meal();
        testMeal.setId(1L);
        testMeal.setName("Breakfast");
        testMeal.setDay(testDay);

        testFoodCategory = new FoodCategory("Apple", 0.3, 14.0, 0.2, 52.0);
        testFoodCategory.setId(1L);

        testFood = new Food();
        testFood.setId(1L);
        testFood.setQuantity(100);
        testFood.setCategoryId(1L);
        testFood.setMeal(testMeal);
    }

    @Test
    void dashboard_WithoutAthleteId_ShouldReturnDashboardWithAllAthletes() {
        // Given
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(foodCategoryRepo.findAll()).thenReturn(Arrays.asList(testFoodCategory));
        when(athleteRepo.findByCoach(testCoach)).thenReturn(Arrays.asList(testAthlete));

        // When
        String result = controller.dashboard(null, model, principal);

        // Then
        assertEquals("index", result);
        verify(model).addAttribute("athletes", Arrays.asList(testAthlete));
        verify(model).addAttribute("selectedAthlete", null);
        verify(model).addAttribute("days", List.of());
        verify(model).addAttribute("meals", List.of());
        verify(model).addAttribute("foods", List.of());
        verify(model).addAttribute(eq("newAthlete"), any(Athlete.class));
        verify(model).addAttribute(eq("newDay"), any(Day.class));
        verify(model).addAttribute(eq("newMeal"), any(Meal.class));
        verify(model).addAttribute(eq("newFood"), any(Food.class));
    }

    @Test
    void dashboard_WithValidAthleteId_ShouldReturnDashboardWithAthleteData() {
        // Given
        Long athleteId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(foodCategoryRepo.findAll()).thenReturn(Arrays.asList(testFoodCategory));
        when(athleteRepo.findByCoach(testCoach)).thenReturn(Arrays.asList(testAthlete));
        when(athleteRepo.findById(athleteId)).thenReturn(Optional.of(testAthlete));
        when(dayRepo.findByAthleteOrderByDateDesc(testAthlete)).thenReturn(Arrays.asList(testDay));
        when(mealRepo.findByDayAthleteOrderByDayDateDesc(testAthlete)).thenReturn(Arrays.asList(testMeal));
        when(foodRepo.findByMealDayAthleteOrderByMealDayDateDesc(testAthlete)).thenReturn(Arrays.asList(testFood));

        // When
        String result = controller.dashboard(athleteId, model, principal);

        // Then
        assertEquals("index", result);
        verify(model).addAttribute("selectedAthlete", testAthlete);
        verify(model).addAttribute("days", Arrays.asList(testDay));
        verify(model).addAttribute("meals", Arrays.asList(testMeal));
        verify(model).addAttribute("foods", Arrays.asList(testFood));
    }

    @Test
    void addAthlete_ShouldSaveAthleteAndRedirect() {
        // Given
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(athleteRepo.save(testAthlete)).thenReturn(testAthlete);

        // When
        String result = controller.addAthlete(testAthlete, principal);

        // Then
        assertEquals("redirect:/", result);
        assertEquals(testCoach, testAthlete.getCoach());
        verify(athleteRepo).save(testAthlete);
    }

    @Test
    void addDay_ShouldSaveDayAndRedirectToAthlete() {
        // Given
        Long athleteId = 1L;
        when(athleteRepo.findById(athleteId)).thenReturn(Optional.of(testAthlete));
        when(dayRepo.save(testDay)).thenReturn(testDay);

        // When
        String result = controller.addDay(testDay, athleteId);

        // Then
        assertEquals("redirect:/?athleteId=1", result);
        assertEquals(testAthlete, testDay.getAthlete());
        verify(dayRepo).save(testDay);
    }

    @Test
    void createDay_WithValidData_ShouldReturnSuccessResponse() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("athleteId", 1L);
        payload.put("dayName", "Tuesday");
        
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(athleteRepo.findById(1L)).thenReturn(Optional.of(testAthlete));
        when(dayRepo.findByAthleteAndDayName(testAthlete, "Tuesday")).thenReturn(Optional.empty());
        when(dayRepo.save(any(Day.class))).thenReturn(testDay);

        // When
        ResponseEntity<?> response = controller.createDay(payload, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dayRepo).save(any(Day.class));
    }

    @Test
    void createDay_WithMissingAthleteId_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("dayName", "Tuesday");

        // When
        ResponseEntity<?> response = controller.createDay(payload, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("athleteId is required", body.get("error"));
    }

    @Test
    void createDay_WithDuplicateDayName_ShouldReturnBadRequest() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("athleteId", 1L);
        payload.put("dayName", "Monday");
        
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(athleteRepo.findById(1L)).thenReturn(Optional.of(testAthlete));
        when(dayRepo.findByAthleteAndDayName(testAthlete, "Monday")).thenReturn(Optional.of(testDay));

        // When
        ResponseEntity<?> response = controller.createDay(payload, principal);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("Day already exists for this athlete", body.get("error"));
    }

    @Test
    void createMeal_WithValidData_ShouldReturnSuccessResponse() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("dayId", 1L);
        payload.put("mealName", "Lunch");
        
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(dayRepo.findById(1L)).thenReturn(Optional.of(testDay));
        when(mealRepo.save(any(Meal.class))).thenReturn(testMeal);

        // When
        ResponseEntity<?> response = controller.createMeal(payload, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mealRepo).save(any(Meal.class));
    }

    @Test
    void createFood_WithValidData_ShouldReturnSuccessResponse() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("mealId", 1L);
        payload.put("quantity", 150);
        payload.put("categoryId", 1L);
        
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(mealRepo.findById(1L)).thenReturn(Optional.of(testMeal));
        when(foodCategoryRepo.findById(1L)).thenReturn(Optional.of(testFoodCategory));
        when(foodRepo.save(any(Food.class))).thenReturn(testFood);

        // When
        ResponseEntity<?> response = controller.createFood(payload, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(foodRepo).save(any(Food.class));
        verify(foodCategoryRepo, atLeastOnce()).findById(1L);
    }

    @Test
    void deleteDay_WithValidId_ShouldReturnSuccessResponse() {
        // Given
        Long dayId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(dayRepo.findById(dayId)).thenReturn(Optional.of(testDay));

        // When
        ResponseEntity<?> response = controller.deleteDay(dayId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dayRepo).delete(testDay);
    }

    @Test
    void deleteMeal_WithValidId_ShouldReturnSuccessResponse() {
        // Given
        Long mealId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(mealRepo.findById(mealId)).thenReturn(Optional.of(testMeal));

        // When
        ResponseEntity<?> response = controller.deleteMeal(mealId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mealRepo).delete(testMeal);
    }

    @Test
    void deleteFood_WithValidId_ShouldReturnSuccessResponse() {
        // Given
        Long foodId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(foodRepo.findById(foodId)).thenReturn(Optional.of(testFood));

        // When
        ResponseEntity<?> response = controller.deleteFood(foodId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(foodRepo).delete(testFood);
    }

    @Test
    void getFoodCategories_ShouldReturnAllCategories() {
        // Given
        when(foodCategoryRepo.findAll()).thenReturn(Arrays.asList(testFoodCategory));

        // When
        ResponseEntity<?> response = controller.getFoodCategories(principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, categories.size());
        assertEquals("Apple", categories.get(0).get("name"));
    }

    @Test
    void getAthletesDays_WithValidAthleteId_ShouldReturnDays() {
        // Given
        Long athleteId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(athleteRepo.findById(athleteId)).thenReturn(Optional.of(testAthlete));
        when(dayRepo.findByAthleteOrderByDateDesc(testAthlete)).thenReturn(Arrays.asList(testDay));

        // When
        ResponseEntity<?> response = controller.getAthletesDays(athleteId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> days = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, days.size());
        assertEquals("Monday", days.get(0).get("dayName"));
    }

    @Test
    void getDayMeals_WithValidDayId_ShouldReturnMeals() {
        // Given
        Long dayId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(dayRepo.findById(dayId)).thenReturn(Optional.of(testDay));
        when(mealRepo.findByDayOrderByIdAsc(testDay)).thenReturn(Arrays.asList(testMeal));

        // When
        ResponseEntity<?> response = controller.getDayMeals(dayId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> meals = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, meals.size());
        assertEquals("Breakfast", meals.get(0).get("name"));
    }

    @Test
    void getMealFoods_WithValidMealId_ShouldReturnFoodsWithCategories() {
        // Given
        Long mealId = 1L;
        when(principal.getName()).thenReturn("testcoach");
        when(coachService.loadCoachByUsername("testcoach")).thenReturn(testCoach);
        when(mealRepo.findById(mealId)).thenReturn(Optional.of(testMeal));
        when(foodRepo.findByMealOrderByIdAsc(testMeal)).thenReturn(Arrays.asList(testFood));
        when(foodCategoryRepo.findById(1L)).thenReturn(Optional.of(testFoodCategory));

        // When
        ResponseEntity<?> response = controller.getMealFoods(mealId, principal);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> foods = (List<Map<String, Object>>) response.getBody();
        assertEquals(1, foods.size());
        
        Map<String, Object> foodData = foods.get(0);
        assertEquals("Apple", foodData.get("categoryName"));
        assertEquals(100, foodData.get("quantity"));
        assertEquals(1L, foodData.get("categoryId"));
    }
} 