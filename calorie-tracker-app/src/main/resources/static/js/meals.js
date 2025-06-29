// ===============================================
// MEAL MANAGEMENT MODULE
// ===============================================

// Meal management state
let mealTabs = {};  // Per day meal tabs: { dayId: { tabs: [], activeTab: null } }
let currentMealDayId = null;

// Initialize meal management when DOM loads
document.addEventListener('DOMContentLoaded', function() {
  console.log('Meals module loaded');
});

// === MEAL MANAGEMENT FUNCTIONS ===

function showMealManagementModal(dayId, dayName) {
  // Always clear any existing modal content first
  clearMealModalContent();
  
  // Check if there are any meals for this day before showing modal
  fetch(`/api/days/${dayId}/meals`)
    .then(response => response.json())
    .then(meals => {
      console.log(`Checking meals for day ${dayName} (ID: ${dayId}):`, meals);
      if (meals && meals.length > 0) {
        // Only show modal if meals exist
        currentMealDayId = dayId;
        const modal = document.getElementById('mealManagementModal');
        const dayNameSpan = document.getElementById('selectedDayName');
        
        dayNameSpan.textContent = dayName;
        modal.style.display = 'block';
        
        // Load the existing meals
        loadExistingMealsForMealModal(dayId, meals);
      } else {
        // No meals exist, hide the modal
        console.log('No meals found for day:', dayName);
        currentMealDayId = null;
        hideMealManagementModal();
      }
    })
    .catch(error => {
      console.error('Error checking meals for day:', error);
      currentMealDayId = null;
      hideMealManagementModal();
    });
}

function clearMealModalContent() {
  // Clear meal tabs from DOM
  const mealTabsNav = document.getElementById('mealTabsNav');
  const mealContent = document.getElementById('mealContent');
  if (mealTabsNav) mealTabsNav.innerHTML = '';
  if (mealContent) mealContent.innerHTML = '';
  
  // Clear meal tabs data for all days
  Object.keys(mealTabs).forEach(key => {
    mealTabs[key].tabs = [];
    mealTabs[key].activeTab = null;
  });
}

function hideMealManagementModal() {
  const modal = document.getElementById('mealManagementModal');
  if (modal) {
    modal.style.display = 'none';
  }
  currentMealDayId = null;
  clearMealModalContent();
}

function addMealForDay(dayId) {
  // Initialize meal tabs for this day if not exists
  if (!mealTabs[dayId]) {
    mealTabs[dayId] = { tabs: [], activeTab: null };
  }

  const dayMealTabs = mealTabs[dayId];
  
  if (dayMealTabs.tabs.length >= 5) {
    showAlert('Maximum 5 meals allowed per day');
    return;
  }

  // Find next available meal
  let nextMeal = 'Breakfast';
  for (let meal of mealOptions) {
    if (!dayMealTabs.tabs.some(tab => tab.name === meal)) {
      nextMeal = meal;
      break;
    }
  }

  createMealInDatabase(dayId, nextMeal);
}

function createMealInDatabase(dayId, mealName) {
  const requestData = {
    dayId: dayId,
    mealName: mealName
  };

  console.log('Creating meal with data:', requestData);

  fetch('/api/meals', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify(requestData)
  })
  .then(response => {
    console.log('Meal response status:', response.status);
    return response.json();
  })
  .then(data => {
    console.log('Meal response data:', data);
    if (data.error) {
      showAlert(data.error);
    } else {
      const dayData = dayTabs.find(d => d.dbId === data.dayId);
      if (dayData) {
        const modal = document.getElementById('mealManagementModal');
        
        // Check if modal is already showing for this day
        if (modal.style.display === 'block' && currentMealDayId === data.dayId) {
          // Modal is already showing, just add the new meal tab to existing ones
          addMealTabFromDB(data.dayId, data.id, data.name, true);
        } else {
          // Modal is not showing or showing different day, reload all meals
          showMealManagementModal(data.dayId, dayData.name);
        }
      }
    }
  })
  .catch(error => {
    console.error('Error creating meal:', error);
    showAlert('Error creating meal: ' + error.message);
  });
}

function addMealTabFromDB(dayId, mealDbId, mealName, activate = false) {
  // Initialize meal tabs for this day if not exists
  if (!mealTabs[dayId]) {
    mealTabs[dayId] = { tabs: [], activeTab: null };
  }

  const dayMealTabs = mealTabs[dayId];
  const mealTabId = 'meal-tab-' + mealDbId;
  
  const mealTab = {
    id: mealTabId,
    dbId: mealDbId,
    name: mealName,
    dayId: dayId,
    element: null,
    contentElement: null
  };

  // Create meal tab button
  const mealTabButton = document.createElement('button');
  mealTabButton.className = 'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm focus:outline-none';
  mealTabButton.setAttribute('data-meal-tab-id', mealTabId);
  mealTabButton.textContent = mealName;

  // Create meal tab content
  const mealTabContent = document.createElement('div');
  mealTabContent.className = activate ? '' : 'hidden';
  mealTabContent.setAttribute('data-meal-tab-content', mealTabId);
  mealTabContent.innerHTML = createMealContent(mealTabId, mealName, mealDbId, dayId);

  // Add to DOM - use the meal modal elements
  const mealTabsNav = document.getElementById('mealTabsNav');
  const mealContent = document.getElementById('mealContent');
  mealTabsNav.appendChild(mealTabButton);
  mealContent.appendChild(mealTabContent);

  // Store references
  mealTab.element = mealTabButton;
  mealTab.contentElement = mealTabContent;
  dayMealTabs.tabs.push(mealTab);

  // Add click handler
  mealTabButton.addEventListener('click', () => activateMealTab(dayId, mealTabId));

  // Activate this tab if requested
  if (activate) {
    activateMealTab(dayId, mealTabId);
  }

  // Add meal selector change handler
  const mealSelect = mealTabContent.querySelector('.meal-selector');
  mealSelect.addEventListener('change', function() {
    const newMealName = this.value;
    // Check if this meal is already used by another tab in this day
    if (dayMealTabs.tabs.some(t => t.id !== mealTabId && t.name === newMealName)) {
      showAlert('This meal is already selected in another tab');
      this.value = mealTab.name; // Reset to previous value
      return;
    }
    
    // Update in database
    updateMealInDatabase(mealDbId, newMealName);
    mealTab.name = newMealName;
    mealTabButton.textContent = newMealName;
  });

  // Add remove button handler
  const removeMealBtn = mealTabContent.querySelector('.remove-meal-btn');
  removeMealBtn.addEventListener('click', () => {
    deleteMealFromDatabase(mealDbId);
    removeMealTab(dayId, mealTabId);
  });

  // Add food button handler
  const addFoodBtn = mealTabContent.querySelector('.add-food-btn');
  addFoodBtn.addEventListener('click', () => {
    addFoodForMeal(mealDbId);
  });
}

function createMealContent(mealTabId, mealName, mealDbId, dayId) {
  return `
    <div class="space-y-4">
      <div class="flex items-center space-x-4">
        <label class="text-sm font-medium text-gray-700">Meal:</label>
        <select class="meal-selector px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500">
          ${mealOptions.map(meal => `<option value="${meal}" ${meal === mealName ? 'selected' : ''}>${meal}</option>`).join('')}
        </select>
        <button class="remove-meal-btn px-3 py-1 bg-red-500 text-white text-sm rounded hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2">
          Remove
        </button>
        <span class="meal-nutrition-display text-red-600 text-xs font-medium">Loading nutrition...</span>
      </div>
      <div class="flex justify-end">
        <button class="add-food-btn px-4 py-2 bg-orange-400 text-white rounded hover:bg-orange-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2" data-meal-id="${mealDbId}">
          Add Food
        </button>
      </div>
    </div>
  `;
}

function activateMealTab(dayId, mealTabId) {
  const dayMealTabs = mealTabs[dayId];
  if (!dayMealTabs) return;

  // Deactivate all meal tabs for this day
  dayMealTabs.tabs.forEach(tab => {
    tab.element.className = 'whitespace-nowrap py-2 px-1 border-b-2 border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 font-medium text-sm focus:outline-none';
    tab.contentElement.classList.add('hidden');
  });

  // Activate selected meal tab
  const activeMealTab = dayMealTabs.tabs.find(tab => tab.id === mealTabId);
  if (activeMealTab) {
    activeMealTab.element.className = 'whitespace-nowrap py-2 px-1 border-b-2 border-orange-500 text-orange-600 font-medium text-sm focus:outline-none';
    activeMealTab.contentElement.classList.remove('hidden');
    dayMealTabs.activeTab = mealTabId;
    
    // Load nutrition for this meal when activated
    loadMealNutrition(activeMealTab.dbId);
    
    // Show food modal if this meal has foods
    showFoodManagementModal(activeMealTab.dbId, activeMealTab.name);
  }
}

function updateMealInDatabase(mealId, newMealName) {
  const requestData = {
    mealName: newMealName
  };

  fetch(`/api/meals/${mealId}`, {
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
    }
  })
  .catch(error => {
    console.error('Error updating meal:', error);
    showAlert('Error updating meal');
  });
}

function deleteMealFromDatabase(mealId) {
  fetch(`/api/meals/${mealId}`, {
    method: 'DELETE',
    headers: {
      [csrfHeader]: csrfToken
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.error) {
      showAlert(data.error);
    }
  })
  .catch(error => {
    console.error('Error deleting meal:', error);
    showAlert('Error deleting meal');
  });
}

function removeMealTab(dayId, mealTabId) {
  const dayMealTabs = mealTabs[dayId];
  if (!dayMealTabs) return;

  const mealTabIndex = dayMealTabs.tabs.findIndex(tab => tab.id === mealTabId);
  if (mealTabIndex === -1) return;

  const mealTab = dayMealTabs.tabs[mealTabIndex];
  
  // Remove from DOM
  mealTab.element.remove();
  mealTab.contentElement.remove();
  
  // Remove from array
  dayMealTabs.tabs.splice(mealTabIndex, 1);

  // If this was the active tab and there are other tabs, activate another one
  if (dayMealTabs.activeTab === mealTabId && dayMealTabs.tabs.length > 0) {
    activateMealTab(dayId, dayMealTabs.tabs[0].id);
  } else if (dayMealTabs.tabs.length === 0) {
    dayMealTabs.activeTab = null;
  }
}

function loadExistingMealsForMealModal(dayId, meals = null) {
  console.log('Loading existing meals for day:', dayId);
  
  // Clear existing meal tabs from DOM
  const mealTabsNav = document.getElementById('mealTabsNav');
  const mealContent = document.getElementById('mealContent');
  mealTabsNav.innerHTML = '';
  mealContent.innerHTML = '';
  
  // Reset meal tabs data for ALL days to prevent cross-contamination
  Object.keys(mealTabs).forEach(key => {
    mealTabs[key].tabs = [];
    mealTabs[key].activeTab = null;
  });
  
  // Initialize fresh meal tabs data for this specific day
  if (!mealTabs[dayId]) {
    mealTabs[dayId] = { tabs: [], activeTab: null };
  } else {
    mealTabs[dayId].tabs = [];
    mealTabs[dayId].activeTab = null;
  }
  
  if (meals) {
    // Use provided meals data
    console.log('Found meals for day', dayId, ':', meals);
    meals.forEach((meal, index) => {
      console.log('Adding meal tab:', meal);
      addMealTabFromDB(dayId, meal.id, meal.name, index === 0);
    });
  } else {
    // Fetch meals from API (fallback)
    fetch(`/api/days/${dayId}/meals`)
      .then(response => response.json())
      .then(meals => {
        console.log('Found meals for day', dayId, ':', meals);
        meals.forEach((meal, index) => {
          console.log('Adding meal tab:', meal);
          addMealTabFromDB(dayId, meal.id, meal.name, index === 0);
        });
      })
      .catch(error => {
        console.error('Error loading meals:', error);
      });
  }
}

// Meal nutrition functions
function loadMealNutrition(mealId) {
  fetch(`/api/meals/${mealId}/nutrition`)
    .then(response => response.json())
    .then(data => {
      if (data.error) {
        console.error('Error loading meal nutrition:', data.error);
        return;
      }
      updateMealNutritionDisplay(mealId, data.protein, data.carbs, data.fat, data.kcal);
    })
    .catch(error => {
      console.error('Error loading meal nutrition:', error);
    });
}

function updateMealNutritionDisplay(mealId, protein, carbs, fat, kcal) {
  // Find the active meal tab and update nutrition info in its content
  Object.values(mealTabs).forEach(dayMealTabs => {
    dayMealTabs.tabs.forEach(tab => {
      if (tab.dbId === mealId && !tab.contentElement.classList.contains('hidden')) {
        const nutritionSpan = tab.contentElement.querySelector('.meal-nutrition-display');
        if (nutritionSpan) {
          nutritionSpan.textContent = `g prot: ${protein}, g carb: ${carbs}, g fat: ${fat}, kcal = ${kcal}`;
        }
      }
    });
  });
}

function refreshMealsNutritionForDay(dayId) {
  // Refresh nutrition for all meals in this day
  if (mealTabs[dayId]) {
    mealTabs[dayId].tabs.forEach(mealTab => {
      loadMealNutrition(mealTab.dbId);
    });
  }
}

function refreshNutritionForMeal(mealId) {
  // Refresh nutrition for the meal itself
  loadMealNutrition(mealId);
  
  // Find the day this meal belongs to and refresh day nutrition
  Object.keys(mealTabs).forEach(dayId => {
    const dayMealTabs = mealTabs[dayId];
    if (dayMealTabs.tabs.some(tab => tab.dbId === mealId)) {
      // Call day nutrition refresh (days.js will handle this)
      if (typeof loadDayNutrition === 'function') {
        loadDayNutrition(parseInt(dayId));
      }
    }
  });
} 