package org.example.config;

import org.example.foodcategories.FoodCategory;
import org.example.model.Athlete;
import org.example.model.Coach;
import org.example.repository.AthleteRepository;
import org.example.repository.CoachRepository;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FoodCategoryRepository foodCategoryRepository;
    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            FoodCategoryRepository foodCategoryRepository,
            CoachRepository coachRepository,
            AthleteRepository athleteRepository,
            PasswordEncoder passwordEncoder) {
        this.foodCategoryRepository = foodCategoryRepository;
        this.coachRepository = coachRepository;
        this.athleteRepository = athleteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeFoodCategories();
        initializeDemoData();
    }

    private void initializeFoodCategories() {
        // Check if food categories already exist
        if (foodCategoryRepository.count() == 0) {
            System.out.println("Initializing food categories...");
            
            // Create the three food categories with nutritional values
            FoodCategory potato = new FoodCategory("potato", 2.0, 80.0, 0.5, 18.0);
            FoodCategory milk = new FoodCategory("milk", 3.2, 61.0, 3.3, 4.8);
            FoodCategory beef = new FoodCategory("beef", 26.0, 260.0, 10.0, 0.0);
            
            foodCategoryRepository.save(potato);
            foodCategoryRepository.save(milk);
            foodCategoryRepository.save(beef);
            
            System.out.println("Food categories initialized successfully!");
        } else {
            System.out.println("Food categories already exist, skipping initialization.");
        }
    }

    private void initializeDemoData() {
        // Check if any coaches exist
        if (coachRepository.count() == 0) {
            System.out.println("Initializing demo coach and athlete...");

            // Create demo coach
            Coach demoCoach = new Coach();
            demoCoach.setUsername("Mathieu");
            demoCoach.setPassword(passwordEncoder.encode("Mathieu"));
            coachRepository.save(demoCoach);

            // Create demo athlete
            Athlete demoAthlete = new Athlete();
            demoAthlete.setName("John Smith");
            demoAthlete.setWeight(75.0);
            demoAthlete.setSize(1.80);
            demoAthlete.setCoach(demoCoach);
            athleteRepository.save(demoAthlete);

            System.out.println("Demo coach and athlete initialized successfully!");
        } else {
            System.out.println("Coach data already exists, skipping demo data initialization.");
        }
    }
} 