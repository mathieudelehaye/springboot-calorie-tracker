// ===============================================
// DAY MANAGEMENT MODULE
// ===============================================

// Day management state
let dayTabs = [];
let activeTab = null;

// Initialize day management when DOM loads
document.addEventListener('DOMContentLoaded', function() {
  console.log('Days module loaded');
  
  // Initialize day management if athlete is selected
  if (selectedAthleteId) {
    initializeDayManagement();
  }
});

function initializeDayManagement() {
  // Add Day button functionality
  const addDayBtn = document.getElementById('addDayBtn');
  if (addDayBtn) {
    addDayBtn.addEventListener('click', function() {
      if (dayTabs.length >= 8) {
        showAlert('Maximum 8 days allowed');
        return;
      }
      
      // Find next available day
      let nextDay = 'Monday';
      for (let day of dayOptions) {
        if (!dayTabs.some(tab => tab.name === day)) {
          nextDay = day;
          break;
        }
      }
      
      createDayInDatabase(nextDay);
    });
  }
  
  // Load existing days
  loadExistingDays();
}

// Load existing days from database
function loadExistingDays() {
  fetch(`/api/athletes/${selectedAthleteId}/days`)
    .then(response => response.json())
    .then(days => {
      dayTabs = []; // Clear existing tabs
      document.getElementById('dayTabsNav').innerHTML = '';
      document.getElementById('dayContent').innerHTML = '';
      
      days.forEach(day => {
        addDayTabFromDB(day.id, day.dayName, false);
      });
      
      // Activate first tab if any exist
      if (dayTabs.length > 0) {
        activateTab(dayTabs[0].id);
      }
    })
    .catch(error => {
      console.error('Error loading days:', error);
    });
}

// Create a new day in the database
function createDayInDatabase(dayName) {
  if (!selectedAthleteId) {
    showAlert('No athlete selected');
    return;
  }

  const requestData = {
    athleteId: selectedAthleteId,
    dayName: dayName
  };

  console.log('Creating day with data:', requestData);

  fetch('/api/days', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify(requestData)
  })
  .then(response => {
    console.log('Response status:', response.status);
    return response.json();
  })
  .then(data => {
    console.log('Response data:', data);
    if (data.error) {
      showAlert(data.error);
    } else {
      addDayTabFromDB(data.id, data.dayName, true);
    }
  })
  .catch(error => {
    console.error('Error creating day:', error);
    showAlert('Error creating day: ' + error.message);
  });
}

// Update day name in database
function updateDayInDatabase(dayId, newDayName) {
  const requestData = {
    dayName: newDayName
  };

  fetch(`/api/days/${dayId}`, {
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
      // Revert the change
      const tab = dayTabs.find(t => t.dbId === dayId);
      if (tab) {
        const daySelect = tab.contentElement.querySelector('.day-selector');
        daySelect.value = tab.name;
      }
    } else {
      // Update successful - tab name already updated in UI
      const tab = dayTabs.find(t => t.dbId === dayId);
      if (tab) {
        tab.name = data.dayName;
      }
    }
  })
  .catch(error => {
    console.error('Error updating day:', error);
    showAlert('Error updating day');
  });
}

// Delete day from database
function deleteDayFromDatabase(dayId) {
  fetch(`/api/days/${dayId}`, {
    method: 'DELETE',
    headers: {
      [csrfHeader]: csrfToken
    }
  })
  .then(response => response.json())
  .then(data => {
    if (data.error) {
      showAlert(data.error);
    } else {
      // Day successfully deleted - tab already removed from UI
    }
  })
  .catch(error => {
    console.error('Error deleting day:', error);
    showAlert('Error deleting day');
  });
}

function addDayTabFromDB(dbId, dayName, activate = false) {
  const tabId = 'tab-' + dbId;
  const tab = {
    id: tabId,
    dbId: dbId, // Database ID
    name: dayName,
    element: null,
    contentElement: null
  };

  // Create tab button
  const tabButton = document.createElement('button');
  tabButton.className = 'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm focus:outline-none';
  tabButton.setAttribute('data-tab-id', tabId);
  tabButton.textContent = dayName;
  
  // Create tab content
  const tabContent = document.createElement('div');
  tabContent.className = activate ? '' : 'hidden';
  tabContent.setAttribute('data-tab-content', tabId);
  tabContent.innerHTML = createDayContent(tabId, dayName);

  // Add to DOM
  document.getElementById('dayTabsNav').appendChild(tabButton);
  document.getElementById('dayContent').appendChild(tabContent);

  // Store references
  tab.element = tabButton;
  tab.contentElement = tabContent;
  dayTabs.push(tab);

  // Add click handler
  tabButton.addEventListener('click', () => activateTab(tabId));

  // Activate this tab if requested
  if (activate) {
    activateTab(tabId);
  }

  // Add day selector change handler
  const daySelect = tabContent.querySelector('.day-selector');
  daySelect.addEventListener('change', function() {
    const newDayName = this.value;
    // Check if this day is already used by another tab
    if (dayTabs.some(t => t.id !== tabId && t.name === newDayName)) {
      showAlert('This day is already selected in another tab');
      this.value = tab.name; // Reset to previous value
      return;
    }
    
    // Update in database
    updateDayInDatabase(dbId, newDayName);
    tab.name = newDayName;
    tabButton.textContent = newDayName;
  });

  // Add remove button handler
  const removeBtn = tabContent.querySelector('.remove-day-btn');
  removeBtn.addEventListener('click', () => {
    deleteDayFromDatabase(dbId);
    removeDayTab(tabId);
  });

  // Add meal button handler
  const addMealBtn = tabContent.querySelector('.add-meal-for-day-btn');
  addMealBtn.addEventListener('click', () => {
    addMealForDay(dbId);
  });
}

function createDayContent(tabId, dayName) {
  return `
    <div class="space-y-4">
      <div class="flex items-center space-x-4">
        <label class="text-sm font-medium text-gray-700">Day:</label>
        <select class="day-selector px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500">
          ${dayOptions.map(day => `<option value="${day}" ${day === dayName ? 'selected' : ''}>${day}</option>`).join('')}
        </select>
        <button class="remove-day-btn px-3 py-1 bg-red-500 text-white text-sm rounded hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2">
          Remove
        </button>
        <span class="day-nutrition-display text-red-600 text-xs font-medium">Loading nutrition...</span>
      </div>
      <div class="flex justify-end">
        <button class="add-meal-for-day-btn px-4 py-2 bg-orange-400 text-white rounded hover:bg-orange-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2">
          Add Meal
        </button>
      </div>
    </div>
  `;
}

function activateTab(tabId) {
  // Deactivate all tabs
  dayTabs.forEach(tab => {
    tab.element.className = 'whitespace-nowrap py-2 px-1 border-b-2 border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 font-medium text-sm focus:outline-none';
    tab.contentElement.classList.add('hidden');
  });

  // Activate selected tab
  const activeTabObj = dayTabs.find(tab => tab.id === tabId);
  if (activeTabObj) {
    activeTabObj.element.className = 'whitespace-nowrap py-2 px-1 border-b-2 border-indigo-500 text-indigo-600 font-medium text-sm focus:outline-none';
    activeTabObj.contentElement.classList.remove('hidden');
    activeTab = tabId;
    
    // Load nutrition for this day when activated
    loadDayNutrition(activeTabObj.dbId);
    
    // Show meal management modal for this day
    showMealManagementModal(activeTabObj.dbId, activeTabObj.name);
  }
}

function removeDayTab(tabId) {
  const tabIndex = dayTabs.findIndex(tab => tab.id === tabId);
  if (tabIndex === -1) return;

  const tab = dayTabs[tabIndex];
  
  // Remove from DOM
  tab.element.remove();
  tab.contentElement.remove();
  
  // Remove from array
  dayTabs.splice(tabIndex, 1);

  // If this was the active tab, activate another one
  if (activeTab === tabId) {
    if (dayTabs.length > 0) {
      activateTab(dayTabs[0].id);
    } else {
      activeTab = null;
      hideMealManagementModal();
    }
  }
}

// Day nutrition functions
function loadDayNutrition(dayId) {
  fetch(`/api/days/${dayId}/nutrition`)
    .then(response => response.json())
    .then(data => {
      if (data.error) {
        console.error('Error loading day nutrition:', data.error);
        return;
      }
      updateDayNutritionDisplay(dayId, data.protein, data.carbs, data.fat, data.kcal);
    })
    .catch(error => {
      console.error('Error loading day nutrition:', error);
    });
}

function updateDayNutritionDisplay(dayId, protein, carbs, fat, kcal) {
  // Find the active day tab and update nutrition info in its content
  const dayTab = dayTabs.find(tab => tab.dbId === dayId);
  if (dayTab && !dayTab.contentElement.classList.contains('hidden')) {
    const nutritionSpan = dayTab.contentElement.querySelector('.day-nutrition-display');
    if (nutritionSpan) {
      nutritionSpan.textContent = `g prot: ${protein}, g carb: ${carbs}, g fat: ${fat}, kcal = ${kcal}`;
    }
  }
}

function refreshNutritionForDay(dayId) {
  // Refresh nutrition for the day itself
  loadDayNutrition(dayId);
  
  // Refresh nutrition for all meals in this day (meals.js will handle this)
  if (typeof refreshMealsNutritionForDay === 'function') {
    refreshMealsNutritionForDay(dayId);
  }
} 