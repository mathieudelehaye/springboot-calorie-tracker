// ===============================================
// FOOD ITEMS MANAGEMENT MODULE
// ===============================================

// Food management state
let currentFoodMealId = null;

// Initialize food management when DOM loads
document.addEventListener('DOMContentLoaded', function() {
  console.log('Food items module loaded');
});

// === FOOD MODAL MANAGEMENT FUNCTIONS ===

function showFoodManagementModal(mealId, mealName) {
  // Check if there are any foods for this meal before showing modal
  fetch(`/api/meals/${mealId}/foods`)
    .then(response => response.json())
    .then(foods => {
      console.log(`Checking foods for meal ${mealName} (ID: ${mealId}):`, foods);
      if (foods && foods.length > 0) {
        // Only show modal if foods exist
        currentFoodMealId = mealId;
        const modal = document.getElementById('foodManagementModal');
        const mealNameSpan = document.getElementById('selectedMealName');
        
        mealNameSpan.textContent = mealName;
        modal.style.display = 'block';
        
        // Load the existing foods
        loadFoodsIntoFoodModal(foods);
      } else {
        // No foods exist, hide the modal
        console.log('No foods found for meal:', mealName);
        currentFoodMealId = null;
        hideFoodManagementModal();
      }
    })
    .catch(error => {
      console.error('Error checking foods for meal:', error);
      currentFoodMealId = null;
      hideFoodManagementModal();
    });
}

function hideFoodManagementModal() {
  const modal = document.getElementById('foodManagementModal');
  if (modal) {
    modal.style.display = 'none';
  }
  currentFoodMealId = null;
  clearFoodModalContent();
}

function clearFoodModalContent() {
  const tableBody = document.getElementById('foodTableBody');
  if (tableBody) {
    tableBody.innerHTML = '';
  }
}

function loadFoodsIntoFoodModal(foods) {
  clearFoodModalContent();
  foods.forEach(food => {
    addFoodToFoodModal(food.id, food.categoryId, food.categoryName, food.quantity,
                     food.prot || 0, food.carb || 0, food.fat || 0, food.kcal || 0, food.gTot || 0);
  });
}

function addFoodToFoodModal(foodId, categoryId, categoryName, quantity, prot, carb, fat, kcal, gTot) {
  const tableBody = document.getElementById('foodTableBody');
  const row = document.createElement('tr');
  row.className = 'food-row';
  row.setAttribute('data-food-id', foodId);

  // Create food categories dropdown options
  const categoryOptions = foodCategories.map(cat => 
    `<option value="${cat.id}" ${cat.id === categoryId ? 'selected' : ''}>${cat.name}</option>`
  ).join('');

  row.innerHTML = `
    <td class="px-6 py-4 whitespace-nowrap">
      <select class="category-select w-32 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 text-sm">
        ${categoryOptions}
      </select>
    </td>
    <td class="px-6 py-4 whitespace-nowrap">
      <input type="number" 
             class="quantity-input w-24 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 text-sm" 
             value="${quantity}" 
             min="1" 
             step="1">
    </td>
    <td class="px-6 py-4 whitespace-nowrap">
      <div class="flex items-center justify-between">
        <span id="nutrition-${foodId}" class="text-red-600 text-xs font-medium"></span>
        <button class="remove-food-btn ml-4 text-red-600 hover:text-red-900 font-medium">×</button>
      </div>
    </td>
  `;

  tableBody.appendChild(row);

  // Update nutritional display
  updateNutritionalDisplay(foodId, prot, carb, fat, kcal, gTot);

  // Add event listeners
  const categorySelect = row.querySelector('.category-select');
  categorySelect.addEventListener('change', function() {
    updateFoodCategory(foodId, this.value);
  });

  const quantityInput = row.querySelector('.quantity-input');
  quantityInput.addEventListener('change', function() {
    updateFoodQuantity(foodId, this.value);
  });

  const removeBtn = row.querySelector('.remove-food-btn');
  removeBtn.addEventListener('click', () => {
    deleteFoodFromDatabase(foodId, currentFoodMealId);
    row.remove();
    // Check if this was the last food and hide modal if so
    if (tableBody.children.length === 0) {
      hideFoodManagementModal();
    }
  });
}

// === NUTRITIONAL DISPLAY FUNCTIONS ===

function updateNutritionalDisplay(foodId, prot, carb, fat, kcal, gTot) {
  const nutritionSpan = document.getElementById(`nutrition-${foodId}`);
  if (nutritionSpan) {
    nutritionSpan.textContent = `g prot: ${prot}, g carb: ${carb}, g fat: ${fat}, g tot: ${gTot}, kcal = ${kcal}`;
  }
}

// === FOOD MANAGEMENT FUNCTIONS ===

function addFoodForMeal(mealId) {
  // Check which meal tab is currently active to get the meal name
  let currentMealName = 'Meal';
  if (currentMealDayId && mealTabs[currentMealDayId]) {
    const activeMealTab = mealTabs[currentMealDayId].tabs.find(tab => 
      tab.dbId === mealId
    );
    if (activeMealTab) {
      currentMealName = activeMealTab.name;
    }
  }
  
  // Use first food category as default
  const defaultCategoryId = foodCategories.length > 0 ? foodCategories[0].id : null;
  if (!defaultCategoryId) {
    showAlert('No food categories available. Please add food categories first.');
    return;
  }
  createFoodInDatabase(mealId, defaultCategoryId, 1, currentMealName);
}

function createFoodInDatabase(mealId, categoryId, quantity, mealName) {
  const requestData = {
    mealId: mealId,
    categoryId: categoryId,
    quantity: quantity
  };

  console.log('Creating food with data:', requestData);

  fetch('/api/foods', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify(requestData)
  })
  .then(response => {
    console.log('Food response status:', response.status);
    return response.json();
  })
  .then(data => {
    console.log('Food response data:', data);
    if (data.error) {
      showAlert(data.error);
    } else {
      const modal = document.getElementById('foodManagementModal');
      
      // Check if food modal is already showing for this meal
      if (modal.style.display === 'block' && currentFoodMealId === data.mealId) {
        // Modal is already showing, just add the new food to existing ones
        addFoodToFoodModal(data.id, data.categoryId, data.categoryName, data.quantity,
                         data.prot || 0, data.carb || 0, data.fat || 0, data.kcal || 0, data.gTot || 0);
      } else {
        // Modal is not showing or showing different meal, show it with the new food
        currentFoodMealId = data.mealId;
        const mealNameSpan = document.getElementById('selectedMealName');
        
        mealNameSpan.textContent = mealName || 'Meal';
        modal.style.display = 'block';
        
        // Clear and add the new food
        clearFoodModalContent();
        addFoodToFoodModal(data.id, data.categoryId, data.categoryName, data.quantity,
                         data.prot || 0, data.carb || 0, data.fat || 0, data.kcal || 0, data.gTot || 0);
      }
      // Refresh nutrition for the meal and day
      refreshNutritionForMeal(data.mealId);
    }
  })
  .catch(error => {
    console.error('Error creating food:', error);
    showAlert('Error creating food: ' + error.message);
  });
}

function updateFoodCategory(foodId, newCategoryId) {
  const requestData = {
    categoryId: parseInt(newCategoryId)
  };

  fetch(`/api/foods/${foodId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify(requestData)
  })
  .then(response => response.json())
  .then(data => {
    if (data.error) {
      showAlert(data.error);
    } else {
      // Update nutritional display with new data
      updateNutritionalDisplay(foodId, data.prot || 0, data.carb || 0, data.fat || 0, data.kcal || 0, data.gTot || 0);
      // Refresh nutrition for the meal and day
      refreshNutritionForMeal(data.mealId);
    }
  })
  .catch(error => {
    console.error('Error updating food category:', error);
    showAlert('Error updating food category');
  });
}

function updateFoodQuantity(foodId, newQuantity) {
  const requestData = {
    quantity: parseInt(newQuantity)
  };

  fetch(`/api/foods/${foodId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify(requestData)
  })
  .then(response => response.json())
  .then(data => {
    if (data.error) {
      showAlert(data.error);
    } else {
      // Update nutritional display with new data
      updateNutritionalDisplay(foodId, data.prot || 0, data.carb || 0, data.fat || 0, data.kcal || 0, data.gTot || 0);
      // Refresh nutrition for the meal and day
      refreshNutritionForMeal(data.mealId);
    }
  })
  .catch(error => {
    console.error('Error updating food quantity:', error);
    showAlert('Error updating food quantity');
  });
}

function deleteFoodFromDatabase(foodId, mealId = null) {
  fetch(`/api/foods/${foodId}`, {
    method: 'DELETE',
    headers: {
      [csrfHeader]: csrfToken
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.error) {
      showAlert(data.error);
    } else if (mealId) {
      // Refresh nutrition for the meal and day after deletion
      refreshNutritionForMeal(mealId);
    }
  })
  .catch(error => {
    console.error('Error deleting food:', error);
    showAlert('Error deleting food');
  });
}

function loadExistingFoodsForMeal(mealId) {
  console.log('Loading existing foods for meal:', mealId);
  fetch(`/api/meals/${mealId}/foods`)
    .then(response => response.json())
    .then(foods => {
      console.log('Found foods for meal', mealId, ':', foods);
      foods.forEach(food => {
        console.log('Adding food to modal:', food);
        addFoodToFoodModal(food.id, food.categoryId, food.categoryName, food.quantity);
      });
    })
    .catch(error => {
      console.error('Error loading foods:', error);
    });
} 