# 📁 HƯỚNG DẪN CẤU TRÚC THƯ MỤC - AUTH SERVICE

## 🎯 TỔNG QUAN

Dự án được tổ chức theo **chuẩn Enterprise Spring Boot**, tuân theo các nguyên tắc:
- **Separation of Concerns** - Tách biệt trách nhiệm
- **Layered Architecture** - Kiến trúc phân lớp
- **Domain-Driven Design** - Thiết kế hướng miền
- **Clean Code** - Mã nguồn sạch

---

## 📂 CẤU TRÚC TỔNG QUAN

```
smartconnect-auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/smartconnect/auth/    # Source code
│   │   └── resources/                     # Configuration & resources
│   └── test/                              # Test code
├── target/                                # Build output (gitignored)
├── logs/                                  # Application logs (gitignored)
├── .mvn/                                  # Maven wrapper
├── Dockerfile                             # Docker build configuration
├── docker-compose.yml                     # Local development setup
├── pom.xml                                # Maven dependencies
├── .env.example                           # Environment variables template
├── .gitignore                             # Git ignore rules
└── README.md                              # Project documentation
```

---

## 🏗️ CHI TIẾT CẤU TRÚC SOURCE CODE

### 📦 `src/main/java/com/smartconnect/auth/`

#### **1. Root Package**

```
AuthServiceApplication.java    # Main application entry point
```

**Chức năng:**
- Spring Boot application starter
- Main method
- Enable JPA Auditing

---

#### **2. `config/` - Configuration Classes**

```
config/
├── SecurityConfig.java         # Spring Security configuration
├── JwtConfig.java              # JWT configuration
├── RedisConfig.java            # Redis configuration
├── CorsConfig.java             # CORS configuration
├── SwaggerConfig.java          # API documentation configuration
└── DatabaseConfig.java         # Database configuration
```

**Mục đích:**
- Tập trung tất cả configuration
- Bean definitions
- External integrations setup

**Best Practices:**
- Sử dụng `@Configuration` annotation
- Externalize properties với `@ConfigurationProperties`
- Profile-specific configs với `@Profile`

---

#### **3. `controller/` - REST Controllers**

```
controller/
├── AuthController.java         # Authentication endpoints
├── UserController.java         # User management endpoints
└── HealthController.java       # Health check endpoints
```

**Responsibilities:**
- HTTP request handling
- Input validation
- Response formatting
- Exception handling delegation

**Structure:**
```java
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request) {
        // Implementation
    }
}
```

**Rules:**
- Thin controllers - business logic ở Service layer
- Sử dụng `@Valid` cho validation
- Return `ResponseEntity` với proper HTTP status
- Log all requests

---

#### **4. `service/` - Business Logic Layer**

```
service/
├── AuthService.java            # Interface
├── UserService.java            # Interface
├── JwtService.java             # Interface
├── RefreshTokenService.java    # Interface
└── impl/
    ├── AuthServiceImpl.java    # Implementation
    ├── UserServiceImpl.java    # Implementation
    └── JwtServiceImpl.java     # Implementation
```

**Responsibilities:**
- Business logic
- Transaction management
- Service orchestration
- Call to repositories

**Pattern:**
- Interface + Implementation
- `@Service` annotation
- `@Transactional` for transactions

```java
public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    // ...
}

@Service
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    // Implementation
}
```

---

#### **5. `repository/` - Data Access Layer**

```
repository/
├── UserRepository.java
└── RefreshTokenRepository.java
```

**Responsibilities:**
- Database queries
- CRUD operations
- Custom queries

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}
```

**Best Practices:**
- Extend `JpaRepository<Entity, ID>`
- Custom queries với `@Query`
- Method naming conventions
- Use `Optional` for nullable returns

---

#### **6. `model/` - Domain Models**

```
model/
├── entity/
│   ├── BaseEntity.java         # Base class with common fields
│   ├── User.java               # User entity
│   └── RefreshToken.java       # Refresh token entity
└── enums/
    ├── UserRole.java           # User roles enum
    └── TokenType.java          # Token types enum
```

**Entity Guidelines:**
```java
@Entity
@Table(name = "users", indexes = {...})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String username;
    
    // ...
}
```

**Rules:**
- Use Lombok annotations
- Proper JPA annotations
- Index important fields
- Extend BaseEntity for audit fields

---

#### **7. `dto/` - Data Transfer Objects**

```
dto/
├── request/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── RefreshTokenRequest.java
│   ├── ForgotPasswordRequest.java
│   └── ResetPasswordRequest.java
└── response/
    ├── AuthResponse.java
    ├── UserResponse.java
    ├── ApiResponse.java
    └── ErrorResponse.java
```

**Purpose:**
- Decouple API from domain models
- Input validation
- API versioning

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password min 8 characters")
    private String password;
}
```

**Best Practices:**
- Use validation annotations
- Immutable objects (final fields + Builder)
- Separate Request/Response DTOs

---

#### **8. `security/` - Security Components**

```
security/
├── JwtAuthenticationFilter.java         # JWT filter
├── JwtAuthenticationEntryPoint.java     # Auth error handler
├── CustomUserDetailsService.java       # Load user for auth
└── SecurityUtils.java                   # Security utilities
```

**Components:**

**Filter:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // Extract token
        // Validate token
        // Set authentication
    }
}
```

---

#### **9. `exception/` - Exception Handling**

```
exception/
├── GlobalExceptionHandler.java       # Global exception handler
├── ResourceNotFoundException.java
├── BadRequestException.java
├── UnauthorizedException.java
├── UserAlreadyExistsException.java
└── InvalidTokenException.java
```

**Global Handler:**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...) {
        // Handle exception
    }
}
```

---

#### **10. `util/` - Utility Classes**

```
util/
├── Constants.java          # Application constants
├── PasswordUtil.java       # Password utilities
└── DateUtil.java           # Date utilities
```

**Rules:**
- Static utility methods
- Private constructor
- Final class

---

#### **11. `mapper/` - Entity-DTO Mappers**

```
mapper/
├── UserMapper.java
└── AuthMapper.java
```

**Using MapStruct:**
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(RegisterRequest request);
}
```

---

#### **12. `validation/` - Custom Validators**

```
validation/
├── validator/
│   ├── PasswordValidator.java
│   └── EmailValidator.java
└── annotation/
    ├── ValidPassword.java
    └── ValidEmail.java
```

---

### 📋 `src/main/resources/`

```
resources/
├── application.yml             # Main configuration
├── application-dev.yml         # Development profile
├── application-prod.yml        # Production profile
├── application-test.yml        # Test profile
├── db/
│   └── migration/              # Flyway migrations
│       ├── V1__create_users_table.sql
│       ├── V2__create_refresh_tokens_table.sql
│       └── V3__insert_default_users.sql
├── static/                     # Static resources
└── templates/                  # Email templates
```

**Configuration Hierarchy:**
1. `application.yml` - Common config
2. `application-{profile}.yml` - Profile-specific
3. Environment variables - Overrides all

---

### 🧪 `src/test/`

```
test/
└── java/com/smartconnect/auth/
    ├── controller/
    │   └── AuthControllerTest.java
    ├── service/
    │   ├── AuthServiceTest.java
    │   └── UserServiceTest.java
    ├── repository/
    │   └── UserRepositoryTest.java
    ├── integration/
    │   └── AuthIntegrationTest.java
    └── util/
        └── TestUtil.java
```

**Test Types:**
- **Unit Tests**: Test individual components
- **Integration Tests**: Test multiple components
- **Repository Tests**: `@DataJpaTest`
- **Controller Tests**: `@WebMvcTest`

---

## 🔄 LUỒNG XỬ LÝ REQUEST

```
Client Request
    ↓
[Controller]
    ↓ validates input
    ↓ calls service
[Service]
    ↓ business logic
    ↓ calls repository
[Repository]
    ↓ database query
[Database]
    ↑ result
[Repository]
    ↑ entity
[Service]
    ↑ maps to DTO
[Controller]
    ↑ formats response
Client Response
```

---

## 📐 NAMING CONVENTIONS

### Classes

| Type | Suffix | Example |
|------|--------|---------|
| Controller | Controller | `AuthController` |
| Service Interface | Service | `AuthService` |
| Service Impl | ServiceImpl | `AuthServiceImpl` |
| Repository | Repository | `UserRepository` |
| Entity | - | `User` |
| DTO Request | Request | `LoginRequest` |
| DTO Response | Response | `AuthResponse` |
| Exception | Exception | `InvalidTokenException` |
| Config | Config | `SecurityConfig` |
| Util | Util | `PasswordUtil` |

### Methods

| Operation | Prefix | Example |
|-----------|--------|---------|
| Get data | get/find | `getUserById` |
| Save data | save/create | `saveUser` |
| Update data | update | `updateUser` |
| Delete data | delete | `deleteUser` |
| Check existence | exists/is | `existsByEmail` |
| Validate | validate | `validateToken` |

### Variables

- **camelCase** for variables: `accessToken`, `userId`
- **UPPER_SNAKE_CASE** for constants: `JWT_SECRET`, `MAX_ATTEMPTS`
- **lowercase** for packages: `com.smartconnect.auth`

---

## 🎯 BEST PRACTICES

### 1. **Dependency Injection**
```java
// ✅ Constructor injection (recommended)
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final UserRepository userRepository;
}

// ❌ Field injection (avoid)
@Autowired
private UserRepository userRepository;
```

### 2. **Exception Handling**
```java
// ✅ Specific exceptions
throw new ResourceNotFoundException("User", "id", userId);

// ❌ Generic exceptions
throw new Exception("User not found");
```

### 3. **Transaction Management**
```java
// ✅ Service layer
@Service
@Transactional
public class UserServiceImpl { }

// ❌ Controller layer
@Transactional  // Wrong place!
public class UserController { }
```

### 4. **Logging**
```java
// ✅ Proper logging
log.info("User {} logged in successfully", username);
log.error("Failed to process payment", exception);

// ❌ System.out
System.out.println("User logged in");
```

---

## 📊 METRICS & MONITORING

- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Logs**: `logs/auth-service.log`

---

## 🔒 SECURITY CHECKLIST

- ✅ JWT token validation
- ✅ Password hashing (BCrypt)
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection
- ✅ CORS configuration
- ✅ Rate limiting
- ✅ Account locking
- ✅ Secure headers

---

## 📝 TODO: Next Steps

Để hoàn thiện Auth Service, cần implement:

### Priority 1 (Core):
1. ✅ Entity models
2. ✅ Repository interfaces
3. ✅ DTO classes
4. ✅ Exception classes
5. ⏳ Service implementations
6. ⏳ Security configurations
7. ⏳ Controllers

### Priority 2 (Features):
8. ⏳ JWT Service implementation
9. ⏳ Refresh token rotation
10. ⏳ Password reset flow
11. ⏳ Email service integration

### Priority 3 (Enhancement):
12. ⏳ Unit tests
13. ⏳ Integration tests
14. ⏳ API documentation
15. ⏳ Performance optimization

---

**Version:** 1.0.0  
**Last Updated:** November 2025  
**Author:** SmartConnect Team

