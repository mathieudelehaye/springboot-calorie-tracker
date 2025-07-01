package org.example.controller;

import org.example.model.*;
import org.example.repository.*;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.example.foodcategories.FoodCategory;
import org.example.service.CoachUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        // Load all food categories for dropdown
        List<FoodCategory> foodCategories = foodCategoryRepo.findAll();
        model.addAttribute("foodCategories", foodCategories);
        
        // Create a simple JSON string for JavaScript (manual serialization)
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < foodCategories.size(); i++) {
            FoodCategory category = foodCategories.get(i);
            jsonBuilder.append("{")
                    .append("\"id\":").append(category.getId()).append(",")
                    .append("\"name\":\"").append(category.getName()).append("\",")
                    .append("\"prot\":").append(category.getProt() != null ? category.getProt() : 0).append(",")
                    .append("\"kcal\":").append(category.getKcal() != null ? category.getKcal() : 0).append(",")
                    .append("\"fat\":").append(category.getFat() != null ? category.getFat() : 0).append(",")
                    .append("\"carb\":").append(category.getCarb() != null ? category.getCarb() : 0)
                    .append("}");
            if (i < foodCategories.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        String foodCategoriesJsonString = jsonBuilder.toString();
        System.out.println("DEBUG: Food categories JSON string: " + foodCategoriesJsonString);
        model.addAttribute("foodCategoriesJson", foodCategoriesJsonString);
        
        // Always load all athletes for the coach (for athlete selection dropdown)
        List<Athlete> allAthletes = athleteRepo.findByCoach(coach);
        model.addAttribute("athletes", allAthletes);
        
        // Already loaded above - removing duplicate
        
        // Prepare empty objects for forms
        model.addAttribute("newAthlete", new Athlete());
        model.addAttribute("newDay", new Day());
        model.addAttribute("newMeal", new Meal());
        model.addAttribute("newFood", new Food());
        
        // If no athlete is selected but athletes exist, redirect to select the first one
        if (athleteId == null && !allAthletes.isEmpty()) {
            return "redirect:/?athleteId=" + allAthletes.get(0).getId();
        }
        
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

    // === REST API endpoints for day management ===

    /**
     * Create a new day for an athlete
     */
    @PostMapping("/api/days")
    @ResponseBody
    public ResponseEntity<?> createDay(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Object athleteIdObj = payload.get("athleteId");
            Object dayNameObj = payload.get("dayName");
            
            if (athleteIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "athleteId is required"));
            }
            if (dayNameObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "dayName is required"));
            }
            
            Long athleteId = Long.valueOf(athleteIdObj.toString());
            String dayName = dayNameObj.toString();

            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Athlete athlete = athleteRepo.findById(athleteId)
                    .filter(a -> a.getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Athlete not found"));

            // Check if this day name already exists for this athlete
            if (dayRepo.findByAthleteAndDayName(athlete, dayName).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Day already exists for this athlete"));
            }

            Day day = new Day();
            day.setAthlete(athlete);
            day.setDayName(dayName);
            day.setDate(LocalDate.now()); // You might want to make this more specific
            
            Day savedDay = dayRepo.save(day);
            
            return ResponseEntity.ok(Map.of(
                "id", savedDay.getId(),
                "dayName", savedDay.getDayName(),
                "athleteId", savedDay.getAthlete().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update a day's name
     */
    @PutMapping("/api/days/{dayId}")
    @ResponseBody
    public ResponseEntity<?> updateDay(@PathVariable Long dayId, @RequestBody Map<String, Object> payload, Principal principal) {
        try {
            String newDayName = payload.get("dayName").toString();

            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Day day = dayRepo.findById(dayId)
                    .filter(d -> d.getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Day not found"));

            // Check if this day name already exists for this athlete (excluding current day)
            if (dayRepo.findByAthleteAndDayNameAndIdNot(day.getAthlete(), newDayName, dayId).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Day name already exists for this athlete"));
            }

            day.setDayName(newDayName);
            Day savedDay = dayRepo.save(day);
            
            return ResponseEntity.ok(Map.of(
                "id", savedDay.getId(),
                "dayName", savedDay.getDayName(),
                "athleteId", savedDay.getAthlete().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a day
     */
    @DeleteMapping("/api/days/{dayId}")
    @ResponseBody
    public ResponseEntity<?> deleteDay(@PathVariable Long dayId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Day day = dayRepo.findById(dayId)
                    .filter(d -> d.getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Day not found"));

            dayRepo.delete(day);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get days for an athlete
     */
    @GetMapping("/api/athletes/{athleteId}/days")
    @ResponseBody
    public ResponseEntity<?> getAthletesDays(@PathVariable Long athleteId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Athlete athlete = athleteRepo.findById(athleteId)
                    .filter(a -> a.getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Athlete not found"));

            List<Day> days = dayRepo.findByAthleteOrderByDateDesc(athlete);
            
            List<Map<String, Object>> dayData = days.stream()
                    .map(day -> {
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("id", day.getId());
                        dayMap.put("dayName", day.getDayName() != null ? day.getDayName() : "Monday");
                        dayMap.put("date", day.getDate().toString());
                        dayMap.put("athleteId", day.getAthlete().getId());
                        return dayMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(dayData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // === REST API endpoints for meal management ===

    /**
     * Create a new meal for a day
     */
    @PostMapping("/api/meals")
    @ResponseBody
    public ResponseEntity<?> createMeal(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Object dayIdObj = payload.get("dayId");
            Object mealNameObj = payload.get("mealName");
            
            if (dayIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "dayId is required"));
            }
            if (mealNameObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "mealName is required"));
            }
            
            Long dayId = Long.valueOf(dayIdObj.toString());
            String mealName = mealNameObj.toString();

            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Day day = dayRepo.findById(dayId)
                    .filter(d -> d.getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Day not found"));

            // Check if this meal name already exists for this day
            if (mealRepo.findByDayAndName(day, mealName).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Meal already exists for this day"));
            }

            Meal meal = new Meal();
            meal.setDay(day);
            meal.setName(mealName);
            
            Meal savedMeal = mealRepo.save(meal);
            
            return ResponseEntity.ok(Map.of(
                "id", savedMeal.getId(),
                "name", savedMeal.getName(),
                "dayId", savedMeal.getDay().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update a meal's name
     */
    @PutMapping("/api/meals/{mealId}")
    @ResponseBody
    public ResponseEntity<?> updateMeal(@PathVariable Long mealId, @RequestBody Map<String, Object> payload, Principal principal) {
        try {
            String newMealName = payload.get("mealName").toString();

            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Meal meal = mealRepo.findById(mealId)
                    .filter(m -> m.getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            // Check if this meal name already exists for this day (excluding current meal)
            if (mealRepo.findByDayAndNameAndIdNot(meal.getDay(), newMealName, mealId).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Meal name already exists for this day"));
            }

            meal.setName(newMealName);
            Meal savedMeal = mealRepo.save(meal);
            
            return ResponseEntity.ok(Map.of(
                "id", savedMeal.getId(),
                "name", savedMeal.getName(),
                "dayId", savedMeal.getDay().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a meal
     */
    @DeleteMapping("/api/meals/{mealId}")
    @ResponseBody
    public ResponseEntity<?> deleteMeal(@PathVariable Long mealId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Meal meal = mealRepo.findById(mealId)
                    .filter(m -> m.getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            mealRepo.delete(meal);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get meals for a day
     */
    @GetMapping("/api/days/{dayId}/meals")
    @ResponseBody
    public ResponseEntity<?> getDayMeals(@PathVariable Long dayId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Day day = dayRepo.findById(dayId)
                    .filter(d -> d.getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Day not found"));

            List<Meal> meals = mealRepo.findByDayOrderByIdAsc(day);
            
            List<Map<String, Object>> mealData = meals.stream()
                    .map(meal -> {
                        Map<String, Object> mealMap = new HashMap<>();
                        mealMap.put("id", meal.getId());
                        mealMap.put("name", meal.getName());
                        mealMap.put("dayId", meal.getDay().getId());
                        return mealMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(mealData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // === REST API endpoints for food management ===

    /**
     * Create a new food for a meal
     */
    @PostMapping("/api/foods")
    @ResponseBody
    public ResponseEntity<?> createFood(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Object mealIdObj = payload.get("mealId");
            Object categoryIdObj = payload.get("categoryId");
            Object quantityObj = payload.get("quantity");
            
            if (mealIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "mealId is required"));
            }
            
            Long mealId = Long.valueOf(mealIdObj.toString());
            int quantity = quantityObj != null ? Integer.valueOf(quantityObj.toString()) : 1;

            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Meal meal = mealRepo.findById(mealId)
                    .filter(m -> m.getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            // Get the food category ID - use provided categoryId or default to first available
            Long categoryId;
            if (categoryIdObj != null) {
                categoryId = Long.valueOf(categoryIdObj.toString());
                // Verify category exists in food categories database
                foodCategoryRepo.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Food category not found"));
            } else {
                // Default to first food category if none specified
                FoodCategory firstCategory = foodCategoryRepo.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No food categories available"));
                categoryId = firstCategory.getId();
            }

            Food food = new Food();
            food.setMeal(meal);
            food.setQuantity(quantity);
            food.setCategoryId(categoryId);
            
            Food savedFood = foodRepo.save(food);
            
            // Get food category from second database and calculate nutritional values
            FoodCategory foodCategory = foodCategoryRepo.findById(savedFood.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Food category not found"));
            int foodQuantity = savedFood.getQuantity();
            
            float prot = (foodCategory.getProt() != null ? foodCategory.getProt().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float carb = (foodCategory.getCarb() != null ? foodCategory.getCarb().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float fat = (foodCategory.getFat() != null ? foodCategory.getFat().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float kcal = (foodCategory.getKcal() != null ? foodCategory.getKcal().floatValue() : 0.0f) * foodQuantity / 100.0f;
            int gTot = foodQuantity; // Total quantity in grams
            
            return ResponseEntity.ok(Map.of(
                "id", savedFood.getId(),
                "categoryId", savedFood.getCategoryId(),
                "categoryName", foodCategory.getName(),
                "quantity", savedFood.getQuantity(),
                "prot", Math.round(prot * 10.0f) / 10.0f, // Round to 1 decimal place
                "carb", Math.round(carb * 10.0f) / 10.0f,
                "fat", Math.round(fat * 10.0f) / 10.0f,
                "kcal", Math.round(kcal),
                "gTot", gTot,
                "mealId", savedFood.getMeal().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update a food's quantity
     */
    @PutMapping("/api/foods/{foodId}")
    @ResponseBody
    @Transactional("primaryTransactionManager")
    public ResponseEntity<?> updateFood(@PathVariable Long foodId, @RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            
            // First verify access rights
            Food existingFood = foodRepo.findById(foodId)
                    .filter(f -> f.getMeal().getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            // Create updated values but don't modify the loaded entity yet
            Integer newQuantity = null;
            Long newCategoryId = null;
            
            if (payload.containsKey("quantity")) {
                newQuantity = Integer.valueOf(payload.get("quantity").toString());
            }
            
            if (payload.containsKey("categoryId")) {
                newCategoryId = Long.valueOf(payload.get("categoryId").toString());
                // Verify category exists in food categories database
                foodCategoryRepo.findById(newCategoryId)
                        .orElseThrow(() -> new RuntimeException("Food category not found"));
            }

            // Now update only if we have changes
            if (newQuantity != null || newCategoryId != null) {
                if (newQuantity != null) {
                    existingFood.setQuantity(newQuantity);
                }
                if (newCategoryId != null) {
                    existingFood.setCategoryId(newCategoryId);
                }
                foodRepo.save(existingFood);
            }

            Food savedFood = existingFood;
            
            // Get food category from second database
            FoodCategory foodCategory = foodCategoryRepo.findById(savedFood.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Food category not found"));
            int foodQuantity = savedFood.getQuantity();
            
            float prot = (foodCategory.getProt() != null ? foodCategory.getProt().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float carb = (foodCategory.getCarb() != null ? foodCategory.getCarb().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float fat = (foodCategory.getFat() != null ? foodCategory.getFat().floatValue() : 0.0f) * foodQuantity / 100.0f;
            float kcal = (foodCategory.getKcal() != null ? foodCategory.getKcal().floatValue() : 0.0f) * foodQuantity / 100.0f;
            int gTot = foodQuantity; // Total quantity in grams
            
            return ResponseEntity.ok(Map.of(
                "id", savedFood.getId(),
                "categoryId", savedFood.getCategoryId(),
                "categoryName", foodCategory.getName(),
                "quantity", savedFood.getQuantity(),
                "prot", Math.round(prot * 10.0f) / 10.0f, // Round to 1 decimal place
                "carb", Math.round(carb * 10.0f) / 10.0f,
                "fat", Math.round(fat * 10.0f) / 10.0f,
                "kcal", Math.round(kcal),
                "gTot", gTot,
                "mealId", savedFood.getMeal().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a food
     */
    @DeleteMapping("/api/foods/{foodId}")
    @ResponseBody
    public ResponseEntity<?> deleteFood(@PathVariable Long foodId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Food food = foodRepo.findById(foodId)
                    .filter(f -> f.getMeal().getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Food not found"));

            foodRepo.delete(food);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get foods for a meal
     */
    @GetMapping("/api/meals/{mealId}/foods")
    @ResponseBody
    public ResponseEntity<?> getMealFoods(@PathVariable Long mealId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Meal meal = mealRepo.findById(mealId)
                    .filter(m -> m.getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            List<Food> foods = foodRepo.findByMealOrderByIdAsc(meal);
            
            List<Map<String, Object>> foodData = foods.stream()
                    .map(food -> {
                        Map<String, Object> foodMap = new HashMap<>();
                        foodMap.put("id", food.getId());
                        foodMap.put("categoryId", food.getCategoryId());
                        foodMap.put("quantity", food.getQuantity());
                        
                        // Get food category from second database
                        FoodCategory foodCategory = foodCategoryRepo.findById(food.getCategoryId())
                                .orElse(null);
                        
                        if (foodCategory != null) {
                            foodMap.put("categoryName", foodCategory.getName());
                            
                            // Calculate nutritional values (quantity/100 * nutritional_value_per_100g)
                            int foodQuantity = food.getQuantity();
                            
                            float prot = (foodCategory.getProt() != null ? foodCategory.getProt().floatValue() : 0.0f) * foodQuantity / 100.0f;
                            float carb = (foodCategory.getCarb() != null ? foodCategory.getCarb().floatValue() : 0.0f) * foodQuantity / 100.0f;
                            float fat = (foodCategory.getFat() != null ? foodCategory.getFat().floatValue() : 0.0f) * foodQuantity / 100.0f;
                            float kcal = (foodCategory.getKcal() != null ? foodCategory.getKcal().floatValue() : 0.0f) * foodQuantity / 100.0f;
                            int gTot = foodQuantity; // Total quantity in grams
                            
                            foodMap.put("prot", Math.round(prot * 10.0f) / 10.0f); // Round to 1 decimal place
                            foodMap.put("carb", Math.round(carb * 10.0f) / 10.0f);
                            foodMap.put("fat", Math.round(fat * 10.0f) / 10.0f);
                            foodMap.put("kcal", Math.round(kcal));
                            foodMap.put("gTot", gTot);
                        } else {
                            foodMap.put("categoryName", "Unknown Category");
                            foodMap.put("prot", 0.0f);
                            foodMap.put("carb", 0.0f);
                            foodMap.put("fat", 0.0f);
                            foodMap.put("kcal", 0);
                            foodMap.put("gTot", food.getQuantity());
                        }
                        
                        foodMap.put("mealId", food.getMeal().getId());
                        return foodMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(foodData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all food categories for debugging
     */
    @GetMapping("/api/food-categories")
    @ResponseBody
    public ResponseEntity<?> getFoodCategories(Principal principal) {
        try {
            List<FoodCategory> categories = foodCategoryRepo.findAll();
            List<Map<String, Object>> categoryData = categories.stream()
                    .map(category -> {
                        Map<String, Object> categoryMap = new HashMap<>();
                        categoryMap.put("id", category.getId());
                        categoryMap.put("name", category.getName());
                        categoryMap.put("prot", category.getProt());
                        categoryMap.put("kcal", category.getKcal());
                        categoryMap.put("fat", category.getFat());
                        categoryMap.put("carb", category.getCarb());
                        return categoryMap;
                    })
                    .toList();
            
            return ResponseEntity.ok(categoryData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get nutrition information for a specific meal
     */
    @GetMapping("/api/meals/{mealId}/nutrition")
    @ResponseBody
    public ResponseEntity<?> getMealNutrition(@PathVariable Long mealId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Meal meal = mealRepo.findById(mealId)
                    .filter(m -> m.getDay().getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            // Calculate nutrition by fetching food categories from second database
            List<Food> foods = foodRepo.findByMealOrderByIdAsc(meal);
            
            float totalProtein = 0.0f;
            float totalCarbs = 0.0f;
            float totalFat = 0.0f;
            float totalKcal = 0.0f;
            
            for (Food food : foods) {
                FoodCategory category = foodCategoryRepo.findById(food.getCategoryId()).orElse(null);
                if (category != null) {
                    int quantity = food.getQuantity();
                    totalProtein += (category.getProt() != null ? category.getProt() : 0.0f) * quantity / 100.0f;
                    totalCarbs += (category.getCarb() != null ? category.getCarb() : 0.0f) * quantity / 100.0f;
                    totalFat += (category.getFat() != null ? category.getFat() : 0.0f) * quantity / 100.0f;
                    totalKcal += (category.getKcal() != null ? category.getKcal() : 0.0f) * quantity / 100.0f;
                }
            }

            return ResponseEntity.ok(Map.of(
                "mealId", meal.getId(),
                "mealName", meal.getName(),
                "protein", Math.round(totalProtein * 10.0f) / 10.0f,
                "carbs", Math.round(totalCarbs * 10.0f) / 10.0f,
                "fat", Math.round(totalFat * 10.0f) / 10.0f,
                "kcal", Math.round(totalKcal)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get nutrition information for a specific day
     */
    @GetMapping("/api/days/{dayId}/nutrition")
    @ResponseBody
    public ResponseEntity<?> getDayNutrition(@PathVariable Long dayId, Principal principal) {
        try {
            Coach coach = coachService.loadCoachByUsername(principal.getName());
            Day day = dayRepo.findById(dayId)
                    .filter(d -> d.getAthlete().getCoach().equals(coach))
                    .orElseThrow(() -> new RuntimeException("Day not found"));

            // Calculate nutrition by fetching food categories from second database
            List<Meal> meals = mealRepo.findByDayOrderByIdAsc(day);
            
            float totalProtein = 0.0f;
            float totalCarbs = 0.0f;
            float totalFat = 0.0f;
            float totalKcal = 0.0f;
            
            for (Meal meal : meals) {
                List<Food> foods = foodRepo.findByMealOrderByIdAsc(meal);
                for (Food food : foods) {
                    FoodCategory category = foodCategoryRepo.findById(food.getCategoryId()).orElse(null);
                    if (category != null) {
                        int quantity = food.getQuantity();
                        totalProtein += (category.getProt() != null ? category.getProt() : 0.0f) * quantity / 100.0f;
                        totalCarbs += (category.getCarb() != null ? category.getCarb() : 0.0f) * quantity / 100.0f;
                        totalFat += (category.getFat() != null ? category.getFat() : 0.0f) * quantity / 100.0f;
                        totalKcal += (category.getKcal() != null ? category.getKcal() : 0.0f) * quantity / 100.0f;
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                "dayId", day.getId(),
                "dayName", day.getDayName(),
                "protein", Math.round(totalProtein * 10.0f) / 10.0f,
                "carbs", Math.round(totalCarbs * 10.0f) / 10.0f,
                "fat", Math.round(totalFat * 10.0f) / 10.0f,
                "kcal", Math.round(totalKcal)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 
