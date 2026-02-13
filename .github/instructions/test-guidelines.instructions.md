---
applyTo: "**/*Test.java"
---

## Unit Test Requirements

When writing unit tests for this Spring Boot project, please follow these guidelines to ensure consistency and maintainability:

### Test Structure and Naming
1. **Use descriptive test method names** - Follow the pattern `methodName_whenCondition_shouldExpectedBehavior()`
   - Example: `getComment_whenCommentExists_shouldReturnCommentDTO()`
2. **Use JUnit 5** - All tests should use `org.junit.jupiter` annotations
3. **Use @ExtendWith(MockitoExtension.class)** for Mockito integration

### Test Organization (Given-When-Then Pattern)
1. **Given** - Set up test data and mocks
   - Use TestEntityFactory or BaseTestEntityFactory for creating test entities
   - Configure mock behaviors with `when()` statements
2. **When** - Execute the method under test
3. **Then** - Verify the results
   - Use AssertJ assertions (`assertThat()`) for fluent, readable assertions
   - Verify mock interactions when appropriate

### Mocking and Dependencies
1. **Use @Mock for dependencies** - Mock all service dependencies, repositories, and external services
2. **Use @InjectMocks for the class under test** - Automatically inject mocked dependencies
3. **Mock only what's necessary** - Don't over-mock; focus on the behavior being tested

### Assertions
1. **Prefer AssertJ over JUnit assertions** - Use `assertThat()` from `org.assertj.core.api.Assertions`
2. **Test specific properties** - Don't just check if objects are not null; verify actual values
3. **Use assertThatThrownBy** for exception testing
   - Example: `assertThatThrownBy(() -> service.method()).isInstanceOf(NotFoundException.class)`

### Test Coverage
1. **Test happy paths and edge cases** - Cover both successful scenarios and error cases
2. **Aim for 80%+ code coverage** - Focus on service layer methods
3. **Don't test getters/setters or DTOs** - These are excluded from coverage requirements

### Example Unit Test Pattern
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void getUserById_whenUserExists_shouldReturnUserDTO() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = TestEntityFactory.createUser(userId, "testuser");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        UserDTO result = userService.getUserById(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId.toString());
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).findById(userId);
    }
    
    @Test
    void getUserById_whenUserNotFound_shouldThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
```

### What to Avoid
- Don't use `@SpringBootTest` for unit tests (use for integration tests only)
- Don't test Spring framework functionality
- Don't make network calls or database connections in unit tests
- Don't use Thread.sleep() or time-based assertions
- Don't test private methods directly
