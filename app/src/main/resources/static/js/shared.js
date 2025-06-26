// ===============================================
// SHARED CONSTANTS AND UTILITIES
// ===============================================

// Available day options
const dayOptions = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday', 'Everyday'];

// Available meal options
const mealOptions = ['Breakfast', 'Lunch', 'Dinner', 'Brunch', 'Tea'];

// Food categories loaded from backend (will be set in HTML)
let foodCategories = [];

// Global state variables
let selectedAthleteId = null;

// CSRF token for requests
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || 
                 document.querySelector('input[name="_csrf"]')?.value;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || '_csrf';

// Utility function to show alerts
function showAlert(message, type = 'error') {
  // For now just use alert, could be enhanced with better UI
  alert(message);
}

// Initialize shared functionality when DOM loads
document.addEventListener('DOMContentLoaded', function() {
  // Get athlete ID from Thymeleaf first, then URL fallback
  if (window.selectedAthleteIdFromBackend) {
    selectedAthleteId = window.selectedAthleteIdFromBackend;
  } else if (!selectedAthleteId) {
    const urlParams = new URLSearchParams(window.location.search);
    const athleteIdFromUrl = urlParams.get('athleteId');
    if (athleteIdFromUrl) {
      selectedAthleteId = parseInt(athleteIdFromUrl);
    }
  }
  
  // Sync food categories from global window variable
  if (window.foodCategories) {
    foodCategories = window.foodCategories;
  }
  
  console.log('Selected athlete ID:', selectedAthleteId);
  console.log('Food categories loaded:', foodCategories);
}); 