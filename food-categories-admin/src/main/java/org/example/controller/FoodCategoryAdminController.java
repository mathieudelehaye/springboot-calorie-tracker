package org.example.controller;

import org.example.foodcategories.FoodCategory;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/admin/food-categories")
public class FoodCategoryAdminController {

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    @GetMapping
    public String listFoodCategories(Model model) {
        model.addAttribute("foodCategories", foodCategoryRepository.findAll());
        model.addAttribute("newFoodCategory", new FoodCategory());
        return "admin/food-categories";
    }

    @PostMapping
    public String createFoodCategory(@Valid @ModelAttribute("newFoodCategory") FoodCategory foodCategory,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("foodCategories", foodCategoryRepository.findAll());
            return "admin/food-categories";
        }
        
        try {
            foodCategoryRepository.save(foodCategory);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Food category '" + foodCategory.getName() + "' created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating food category: " + e.getMessage());
        }
        
        return "redirect:/admin/food-categories";
    }

    @GetMapping("/edit/{id}")
    public String editFoodCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FoodCategory> foodCategory = foodCategoryRepository.findById(id);
        
        if (foodCategory.isPresent()) {
            model.addAttribute("foodCategory", foodCategory.get());
            return "admin/edit-food-category";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Food category not found!");
            return "redirect:/admin/food-categories";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateFoodCategory(@PathVariable Long id,
                                   @Valid @ModelAttribute("foodCategory") FoodCategory foodCategory,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "admin/edit-food-category";
        }
        
        try {
            foodCategory.setId(id);
            foodCategoryRepository.save(foodCategory);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Food category updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating food category: " + e.getMessage());
        }
        
        return "redirect:/admin/food-categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteFoodCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<FoodCategory> foodCategory = foodCategoryRepository.findById(id);
            if (foodCategory.isPresent()) {
                foodCategoryRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Food category '" + foodCategory.get().getName() + "' deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Food category not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting food category: " + e.getMessage());
        }
        
        return "redirect:/admin/food-categories";
    }
} 