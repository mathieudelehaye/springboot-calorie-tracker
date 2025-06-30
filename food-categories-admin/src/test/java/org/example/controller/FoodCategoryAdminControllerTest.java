package org.example.controller;

import org.example.foodcategories.FoodCategory;
import org.example.repository.foodcategories.FoodCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FoodCategoryAdminControllerTest {

    @Mock
    private FoodCategoryRepository foodCategoryRepository;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private FoodCategoryAdminController controller;

    private MockMvc mockMvc;
    private FoodCategory testFoodCategory;
    private List<FoodCategory> testFoodCategories;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        // Create test data
        testFoodCategory = new FoodCategory("Apple", 0.3, 14.0, 0.2, 52.0);
        testFoodCategory.setId(1L);
        
        FoodCategory banana = new FoodCategory("Banana", 1.1, 23.0, 0.3, 89.0);
        banana.setId(2L);
        
        testFoodCategories = Arrays.asList(testFoodCategory, banana);
    }

    @Test
    void listFoodCategories_ShouldReturnCorrectViewAndModel() {
        // Given
        when(foodCategoryRepository.findAll()).thenReturn(testFoodCategories);

        // When
        String viewName = controller.listFoodCategories(model);

        // Then
        assertEquals("admin/food-categories", viewName);
        verify(model).addAttribute("foodCategories", testFoodCategories);
        verify(model).addAttribute(eq("newFoodCategory"), any(FoodCategory.class));
        verify(foodCategoryRepository).findAll();
    }

    @Test
    void createFoodCategory_WithValidData_ShouldSaveAndRedirect() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(foodCategoryRepository.save(testFoodCategory)).thenReturn(testFoodCategory);

        // When
        String result = controller.createFoodCategory(testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(foodCategoryRepository).save(testFoodCategory);
        verify(redirectAttributes).addFlashAttribute("successMessage", 
            "Food category 'Apple' created successfully!");
    }

    @Test
    void createFoodCategory_WithValidationErrors_ShouldReturnFormWithErrors() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);
        when(foodCategoryRepository.findAll()).thenReturn(testFoodCategories);

        // When
        String result = controller.createFoodCategory(testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("admin/food-categories", result);
        verify(model).addAttribute("foodCategories", testFoodCategories);
        verify(foodCategoryRepository, never()).save(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void createFoodCategory_WithSaveException_ShouldHandleError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(foodCategoryRepository.save(testFoodCategory))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String result = controller.createFoodCategory(testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", 
            "Error creating food category: Database error");
    }

    @Test
    void editFoodCategory_WithValidId_ShouldReturnEditView() {
        // Given
        Long categoryId = 1L;
        when(foodCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testFoodCategory));

        // When
        String result = controller.editFoodCategory(categoryId, model, redirectAttributes);

        // Then
        assertEquals("admin/edit-food-category", result);
        verify(model).addAttribute("foodCategory", testFoodCategory);
        verify(foodCategoryRepository).findById(categoryId);
    }

    @Test
    void editFoodCategory_WithInvalidId_ShouldRedirectWithError() {
        // Given
        Long categoryId = 999L;
        when(foodCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        String result = controller.editFoodCategory(categoryId, model, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Food category not found!");
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void updateFoodCategory_WithValidData_ShouldUpdateAndRedirect() {
        // Given
        Long categoryId = 1L;
        when(bindingResult.hasErrors()).thenReturn(false);
        when(foodCategoryRepository.save(testFoodCategory)).thenReturn(testFoodCategory);

        // When
        String result = controller.updateFoodCategory(categoryId, testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        assertEquals(categoryId, testFoodCategory.getId());
        verify(foodCategoryRepository).save(testFoodCategory);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Food category updated successfully!");
    }

    @Test
    void updateFoodCategory_WithValidationErrors_ShouldReturnEditView() {
        // Given
        Long categoryId = 1L;
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String result = controller.updateFoodCategory(categoryId, testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("admin/edit-food-category", result);
        verify(foodCategoryRepository, never()).save(any());
        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void updateFoodCategory_WithSaveException_ShouldHandleError() {
        // Given
        Long categoryId = 1L;
        when(bindingResult.hasErrors()).thenReturn(false);
        when(foodCategoryRepository.save(testFoodCategory))
            .thenThrow(new RuntimeException("Update failed"));

        // When
        String result = controller.updateFoodCategory(categoryId, testFoodCategory, bindingResult, model, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", 
            "Error updating food category: Update failed");
    }

    @Test
    void deleteFoodCategory_WithValidId_ShouldDeleteAndRedirect() {
        // Given
        Long categoryId = 1L;
        when(foodCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testFoodCategory));

        // When
        String result = controller.deleteFoodCategory(categoryId, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(foodCategoryRepository).deleteById(categoryId);
        verify(redirectAttributes).addFlashAttribute("successMessage", 
            "Food category 'Apple' deleted successfully!");
    }

    @Test
    void deleteFoodCategory_WithInvalidId_ShouldHandleNotFound() {
        // Given
        Long categoryId = 999L;
        when(foodCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        String result = controller.deleteFoodCategory(categoryId, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(foodCategoryRepository, never()).deleteById(any());
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Food category not found!");
    }

    @Test
    void deleteFoodCategory_WithDeleteException_ShouldHandleError() {
        // Given
        Long categoryId = 1L;
        when(foodCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testFoodCategory));
        doThrow(new RuntimeException("Delete failed")).when(foodCategoryRepository).deleteById(categoryId);

        // When
        String result = controller.deleteFoodCategory(categoryId, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/food-categories", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", 
            "Error deleting food category: Delete failed");
    }
} 