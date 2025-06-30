package org.example.foodcategories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodCategoryTest {

    private FoodCategory foodCategory;

    @BeforeEach
    void setUp() {
        foodCategory = new FoodCategory("Apple", 0.3, 14.0, 0.2, 52.0);
    }

    @Test
    void constructor_WithAllParameters_ShouldCreateFoodCategory() {
        // When
        FoodCategory category = new FoodCategory("Banana", 1.1, 23.0, 0.3, 89.0);

        // Then
        assertEquals("Banana", category.getName());
        assertEquals(1.1, category.getProt());
        assertEquals(23.0, category.getCarb());
        assertEquals(0.3, category.getFat());
        assertEquals(89.0, category.getKcal());
        assertNull(category.getId()); // ID should be null until persisted
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyFoodCategory() {
        // When
        FoodCategory category = new FoodCategory();

        // Then
        assertNull(category.getName());
        assertNull(category.getProt());
        assertNull(category.getCarb());
        assertNull(category.getFat());
        assertNull(category.getKcal());
        assertNull(category.getId());
    }

    @Test
    void setName_ShouldUpdateName() {
        // When
        foodCategory.setName("Orange");

        // Then
        assertEquals("Orange", foodCategory.getName());
    }

    @Test
    void setProt_ShouldUpdateProtein() {
        // When
        foodCategory.setProt(1.5);

        // Then
        assertEquals(1.5, foodCategory.getProt());
    }

    @Test
    void setCarb_ShouldUpdateCarbohydrates() {
        // When
        foodCategory.setCarb(20.0);

        // Then
        assertEquals(20.0, foodCategory.getCarb());
    }

    @Test
    void setFat_ShouldUpdateFat() {
        // When
        foodCategory.setFat(0.8);

        // Then
        assertEquals(0.8, foodCategory.getFat());
    }

    @Test
    void setKcal_ShouldUpdateCalories() {
        // When
        foodCategory.setKcal(75.0);

        // Then
        assertEquals(75.0, foodCategory.getKcal());
    }

    @Test
    void setId_ShouldUpdateId() {
        // When
        foodCategory.setId(123L);

        // Then
        assertEquals(123L, foodCategory.getId());
    }

    @Test
    void toString_ShouldReturnCorrectFormat() {
        // Given
        foodCategory.setId(1L);

        // When
        String result = foodCategory.toString();

        // Then
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='Apple'"));
        assertTrue(result.contains("prot=0.3"));
        assertTrue(result.contains("carb=14.0"));
        assertTrue(result.contains("fat=0.2"));
        assertTrue(result.contains("kcal=52.0"));
    }

    @Test
    void nutritionalValues_CanBeNull() {
        // Given
        FoodCategory category = new FoodCategory();

        // When
        category.setName("Water");
        category.setProt(null);
        category.setCarb(null);
        category.setFat(null);
        category.setKcal(0.0);

        // Then
        assertEquals("Water", category.getName());
        assertNull(category.getProt());
        assertNull(category.getCarb());
        assertNull(category.getFat());
        assertEquals(0.0, category.getKcal());
    }

    @Test
    void nutritionalValues_CanBeZero() {
        // Given
        FoodCategory category = new FoodCategory();

        // When
        category.setName("Lettuce");
        category.setProt(0.0);
        category.setCarb(0.0);
        category.setFat(0.0);
        category.setKcal(0.0);

        // Then
        assertEquals("Lettuce", category.getName());
        assertEquals(0.0, category.getProt());
        assertEquals(0.0, category.getCarb());
        assertEquals(0.0, category.getFat());
        assertEquals(0.0, category.getKcal());
    }

    @Test
    void nutritionalValues_CanBeDecimal() {
        // Given
        FoodCategory category = new FoodCategory();

        // When
        category.setName("Avocado");
        category.setProt(2.0);
        category.setCarb(8.5);
        category.setFat(14.7);
        category.setKcal(160.5);

        // Then
        assertEquals(2.0, category.getProt());
        assertEquals(8.5, category.getCarb());
        assertEquals(14.7, category.getFat());
        assertEquals(160.5, category.getKcal());
    }
} 