---
applyTo: "**/controller/*Controller.java"
---

## REST Controller Requirements

When writing REST controllers for this Spring Boot project, please follow these guidelines to ensure consistency and maintainability:

### Controller Structure
1. **Use @RestController** - All controllers should use `@RestController` annotation
2. **Use @RequestMapping** - Define base path using constants from `ApiConstants`
3. **Use @RequiredArgsConstructor** - Enable constructor-based dependency injection with Lombok
4. **Use @Tag** - Document controller with OpenAPI tag annotation

### Dependency Injection
1. **Constructor-based injection** - Use `@RequiredArgsConstructor` with final fields
2. **Inject services, not repositories** - Controllers should only depend on service layer
3. **Keep controllers thin** - Business logic belongs in services, not controllers

### API Documentation
1. **Use @Operation** - Document every endpoint with summary and description
2. **Use @Parameter** - Document path variables and request parameters
3. **Use @ApiResponse** - Document possible response codes (200, 201, 400, 401, 403, 404, 500)
4. **Use @Tag** - Group related endpoints with descriptive tags

### HTTP Methods and Status Codes
1. **GET** - For read operations, return 200 OK
2. **POST** - For create operations, return 201 Created
3. **PUT** - For full updates, return 200 OK
4. **PATCH** - For partial updates, return 200 OK
5. **DELETE** - For delete operations, return 204 No Content
6. **Return ResponseEntity** - Wrap responses in ResponseEntity for proper HTTP status codes

### Security
1. **Use @PreAuthorize** - Secure endpoints with role-based access control
   - Example: `@PreAuthorize("hasAnyRole('ADMIN','USER')")`
2. **Use @CurrentUserId** - Inject current user ID from security context
3. **Hide sensitive parameters** - Use `@Parameter(hidden = true)` for injected user IDs

### RESTful URL Patterns
1. **Use constants** - Define all paths in `ApiConstants` class
2. **Follow REST conventions** - Use plural resource names (e.g., `/users`, `/trips`)
3. **Use path variables** - For resource identifiers (e.g., `/users/{id}`)
4. **Version APIs** - Use `/api/1/` prefix for all endpoints

### Request/Response Handling
1. **Use DTOs only** - Never expose JPA entities in request or response
2. **Use request DTOs** - For POST/PUT/PATCH request bodies
3. **Use response DTOs** - For all response bodies
4. **Validate inputs** - Use `@Valid` annotation for request body validation

### Example Controller Pattern

#### Query Controller (Read Operations)
```java
@RestController
@RequestMapping(ApiConstants.USERS_PATH)
@RequiredArgsConstructor
@Tag(name = "User Queries", description = "Endpoints for retrieving user information")
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping(ApiConstants.USER_BY_ID_ENDPOINT)
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userQueryService.getUserById(id));
    }

    @GetMapping(ApiConstants.ME_SUFFIX)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile of the currently authenticated user")
    public ResponseEntity<UserResponse> getMyUser(
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        return ResponseEntity.ok(userQueryService.getUserById(userId));
    }
}
```

#### Command Controller (Write Operations)
```java
@RestController
@RequestMapping(ApiConstants.TRIPS_PATH)
@RequiredArgsConstructor
@Tag(name = "Trip Commands", description = "Endpoints for trip management")
public class TripCommandController {

    private final TripService tripService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Create new trip", description = "Creates a new trip for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Trip created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid trip data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody CreateTripRequest request,
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        TripResponse response = tripService.createTrip(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Update trip", description = "Updates an existing trip")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequest request,
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        return ResponseEntity.ok(tripService.updateTrip(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Delete trip", description = "Deletes a trip by ID")
    @ApiResponse(responseCode = "204", description = "Trip deleted successfully")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable UUID id,
            @Parameter(hidden = true) @CurrentUserId UUID userId) {
        tripService.deleteTrip(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

### What to Avoid
- Don't implement business logic in controllers
- Don't access repositories directly from controllers
- Don't expose JPA entities in request/response
- Don't use generic Exception handling (use @RestControllerAdvice)
- Don't hardcode strings; use constants
- Don't forget to document with OpenAPI annotations
- Don't use HTTP status 200 for everything; use appropriate codes
