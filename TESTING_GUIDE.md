# Testing Guide

This document provides comprehensive information about the test suite for the SpringBoot Calorie Tracker application.

## Test Overview

The project contains **50+ comprehensive unit test methods** across three modules:

### Diet Manager App (`calorie-tracker-app`) - 32 Tests
- **DashboardControllerTest**: 18 unit tests for REST API endpoints
- **CoachUserDetailsServiceTest**: 4 authentication tests  
- **AthleteTest**: 10 entity relationship tests

### Food Category Admin App (`food-categories-admin`) - 13 Tests  
- **FoodCategoryAdminControllerTest**: 12 unit tests for CRUD operations
- **FoodCategoriesAdminAppTest**: 1 Spring Boot context test

### Shared Module - 9 Tests
- **FoodCategoryTest**: 9 entity validation tests

## Test Status

✅ **All Tests Working**:
- All unit tests pass successfully
- Entity tests work correctly
- Controller tests with mocked dependencies work
- Spring Boot context loading tests pass
- **`./gradlew test` now passes completely**

## Running Tests

### Run All Tests
```bash
# Run complete test suite (all tests pass)
./gradlew test

# Individual modules
./gradlew :calorie-tracker-app:test
./gradlew :food-categories-admin:test
./gradlew :shared:test
```

### Run Individual Test Classes
```bash
# Specific test class
./gradlew :calorie-tracker-app:test --tests="org.example.controller.DashboardControllerTest"
./gradlew :food-categories-admin:test --tests="org.example.controller.FoodCategoryAdminControllerTest"
./gradlew :shared:test --tests="org.example.foodcategories.FoodCategoryTest"
```

### Run Individual Test Methods
```bash
# Specific test method
./gradlew :calorie-tracker-app:test --tests="org.example.controller.DashboardControllerTest.createDay_WithValidData_ShouldReturnSuccessResponse"
```

## Test Configuration

### Test Databases
- **H2 in-memory databases** for fast, isolated testing
- **Primary database**: Main application entities (athletes, days, meals, foods)
- **Food categories database**: Food category lookup data (mocked in tests)
- **Auto-cleanup**: `create-drop` mode ensures clean state for each test

### Test Profiles
Tests use the `test` profile with configurations in:
- `calorie-tracker-app/src/test/resources/application-test.properties`
- `food-categories-admin/src/test/resources/application-test.properties`

### Security Configuration
- Tests run with **security disabled** for easier testing
- Mock users with `@WithMockUser` or `SecurityMockMvcRequestPostProcessors.user()`

## Test Structure

### Unit Tests
Use **Mockito** for dependency mocking:
```java
@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    @Mock private AthleteRepository athleteRepo;
    @Mock private DayRepository dayRepo;
    @InjectMocks private DashboardController controller;
    
    @Test
    void createDay_WithValidData_ShouldReturnSuccessResponse() {
        // Arrange - Given
        when(athleteRepo.findById(1L)).thenReturn(Optional.of(testAthlete));
        
        // Act - When  
        ResponseEntity<?> response = controller.createDay(payload, principal);
        
        // Assert - Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dayRepo).save(any(Day.class));
    }
}
```

### Entity Tests
Test JPA relationships and business logic:
```java
@Test
void addDay_ShouldEstablishBidirectionalRelationship() {
    // Given
    Athlete athlete = new Athlete("John", 70.0, 180.0, coach);
    Day day = new Day();
    
    // When
    athlete.addDay(day);
    
    // Then
    assertTrue(athlete.getDays().contains(day));
    assertEquals(athlete, day.getAthlete());
}
```

## Test Coverage

### Main Application (`calorie-tracker-app`)
- ✅ **REST API Endpoints**: All CRUD operations for athletes, days, meals, foods
- ✅ **Authentication**: Coach user loading and validation
- ✅ **Entity Relationships**: Bidirectional JPA associations
- ✅ **Cross-Database Operations**: Food category lookups (mocked)
- ✅ **HTTP Status Codes**: Proper error handling and success responses

### Admin Application (`food-categories-admin`)  
- ✅ **CRUD Operations**: Create, read, update, delete food categories
- ✅ **Form Validation**: Data validation and error handling
- ✅ **Controller Logic**: Request mapping and response handling

### Shared Module
- ✅ **Entity Validation**: Food category business rules
- ✅ **Data Integrity**: Validation constraints and relationships

## Test Reports

### HTML Reports
```bash
# After running tests, view HTML reports at:
# Main app: calorie-tracker-app/build/reports/tests/test/index.html
# Admin app: food-categories-admin/build/reports/tests/test/index.html
# Shared: shared/build/reports/tests/test/index.html
```

### Console Output
Tests provide detailed console output with:
- ✅ **Pass/Fail Status**: Clear indication of test results
- 📊 **Test Counts**: Number of tests run, passed, failed, skipped
- ⏱️ **Execution Time**: Performance metrics for test suites
- 🔍 **Failure Details**: Stack traces and assertion errors for debugging

## IDE Integration

### IntelliJ IDEA
1. **Right-click** on test class/method → **Run 'TestName'**
2. Use **Ctrl+Shift+F10** to run test under cursor
3. **View** → **Tool Windows** → **Test Results** for detailed output
4. **Green bar**: All tests pass, **Red bar**: Some tests fail

### VS Code  
1. Install **Java Test Runner** extension
2. **Click** the **play button** next to test methods
3. Use **Test Explorer** panel for navigation
4. **Command Palette** → **Java: Run Tests** for full suite

## Best Practices

### Test Naming Convention
- **Method names** describe the scenario: `createDay_WithValidData_ShouldReturnSuccessResponse`
- **Format**: `methodUnderTest_condition_expectedResult`

### AAA Pattern
```java
@Test
void testMethod() {
    // Arrange - Given: Set up test data and mocks
    when(mockService.method()).thenReturn(expectedValue);
    
    // Act - When: Execute the method under test
    Result result = serviceUnderTest.performAction();
    
    // Assert - Then: Verify the results
    assertEquals(expectedValue, result.getValue());
    verify(mockService).method();
}
```

### Mock Strategy
- **Unit Tests**: Mock external dependencies completely
- **Entity Tests**: Pure JPA without Spring context

## Troubleshooting

### Common Issues

**Test Compilation Errors**:
```bash
./gradlew clean build
```

**Mock Verification Failures**:
- Ensure mock setup matches actual method calls
- Use `verify(mock, times(n))` for specific call counts

**H2 Database Issues**:
- Check H2 configuration in `application-test.properties`
- Verify test profile is active

## Continuous Integration

Perfect for CI/CD pipelines:
```bash
# Simple, reliable test execution
./gradlew test

# All tests complete in seconds with 100% pass rate
```

## Future Enhancements
- Add test coverage reporting with JaCoCo
- Implement performance benchmarking
- Add mutation testing for test quality assessment

---

**Total Test Count**: 50+ comprehensive unit test methods  
**Pass Rate**: 100% ✅  
**Coverage**: Core functionality, API endpoints, authentication, data validation  
**Execution**: Fast, reliable, perfect for CI/CD 