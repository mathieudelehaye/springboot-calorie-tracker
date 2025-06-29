package org.example.config;

import org.example.foodcategories.FoodCategory;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FoodCategoryRepository foodCategoryRepository;

    public DataInitializer(FoodCategoryRepository foodCategoryRepository) {
        this.foodCategoryRepository = foodCategoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
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
} 