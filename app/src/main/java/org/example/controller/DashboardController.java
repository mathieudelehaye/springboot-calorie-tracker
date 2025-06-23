package org.example.controller;

import org.example.model.*;
import org.example.repository.*;
import org.example.service.CoachUserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final AthleteRepository athleteRepo;
    private final DayRepository dayRepo;
    private final MealRepository mealRepo;
    private final FoodRepository foodRepo;
    private final FoodCategoryRepository foodCategoryRepo;
    private final CoachUserDetailsService coachService;

    public DashboardController(
            AthleteRepository athleteRepo,
            DayRepository dayRepo,
            MealRepository mealRepo,
            FoodRepository foodRepo,
            FoodCategoryRepository foodCategoryRepo,
            CoachUserDetailsService coachService) {
        this.athleteRepo = athleteRepo;
        this.dayRepo = dayRepo;
        this.mealRepo = mealRepo;
        this.foodRepo = foodRepo;
        this.foodCategoryRepo = foodCategoryRepo;
        this.coachService = coachService;
    }

    /**
     * Dashboard home page - shows all athletes initially, 
     * or data for a selected athlete if athleteId is provided
     */
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) Long athleteId,
            Model model, 
            Principal principal) {
        
        Coach coach = coachService.loadCoachByUsername(principal.getName());
        
        // Always load all athletes for the coach (for athlete selection dropdown)
        List<Athlete> allAthletes = athleteRepo.findByCoach(coach);
        model.addAttribute("athletes", allAthletes);
        
        // Load all food categories (global, used for food creation forms)
        model.addAttribute("foodCategories", foodCategoryRepo.findAll());
        
        // Prepare empty objects for forms
        model.addAttribute("newAthlete", new Athlete());
        model.addAttribute("newDay", new Day());
        model.addAttribute("newMeal", new Meal());
        model.addAttribute("newFood", new Food());
        
        // If an athlete is selected, load their specific data
        if (athleteId != null) {
            Athlete selectedAthlete = athleteRepo.findById(athleteId)
                    .filter(a -> a.getCoach().equals(coach)) // Security: only coach's athletes
                    .orElse(null);
            
            if (selectedAthlete != null) {
                model.addAttribute("selectedAthlete", selectedAthlete);
                
                // Load days for this athlete
                List<Day> athleteDays = dayRepo.findByAthleteOrderByDateDesc(selectedAthlete);
                model.addAttribute("days", athleteDays);
                
                // Load meals for this athlete's days
                List<Meal> athleteMeals = mealRepo.findByDayAthleteOrderByDayDateDesc(selectedAthlete);
                model.addAttribute("meals", athleteMeals);
                
                // Load foods for this athlete's meals
                List<Food> athleteFoods = foodRepo.findByMealDayAthleteOrderByMealDayDateDesc(selectedAthlete);
                model.addAttribute("foods", athleteFoods);
                
                // Pre-populate forms with selected athlete
                Day newDay = new Day();
                newDay.setAthlete(selectedAthlete);
                newDay.setDate(LocalDate.now());
                model.addAttribute("newDay", newDay);
            }
        } else {
            // No athlete selected - show empty collections
            model.addAttribute("selectedAthlete", null);
            model.addAttribute("days", List.of());
            model.addAttribute("meals", List.of());
            model.addAttribute("foods", List.of());
        }
        
        return "index";
    }
    
    /**
     * Handle form submissions and redirect back to dashboard
     */
    @PostMapping("/athletes")
    public String addAthlete(@ModelAttribute("newAthlete") Athlete athlete, Principal principal) {
        Coach coach = coachService.loadCoachByUsername(principal.getName());
        athlete.setCoach(coach);
        athleteRepo.save(athlete);
        return "redirect:/";
    }
    
    @PostMapping("/days")
    public String addDay(@ModelAttribute("newDay") Day day, @RequestParam Long athleteId) {
        Athlete athlete = athleteRepo.findById(athleteId).orElseThrow();
        day.setAthlete(athlete);
        dayRepo.save(day);
        return "redirect:/?athleteId=" + athleteId;
    }
    
    @PostMapping("/meals")
    public String addMeal(@ModelAttribute("newMeal") Meal meal, @RequestParam Long dayId, @RequestParam Long athleteId) {
        Day day = dayRepo.findById(dayId).orElseThrow();
        meal.setDay(day);
        mealRepo.save(meal);
        return "redirect:/?athleteId=" + athleteId;
    }
    
    @PostMapping("/foods")
    public String addFood(@ModelAttribute("newFood") Food food, @RequestParam Long mealId, @RequestParam Long athleteId) {
        Meal meal = mealRepo.findById(mealId).orElseThrow();
        food.setMeal(meal);
        foodRepo.save(food);
        return "redirect:/?athleteId=" + athleteId;
    }
} 