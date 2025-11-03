# 🔐 SmartConnect Auth Service

Authentication & Authorization Microservice for SmartConnect Education Platform.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Security](#security)
- [Deployment](#deployment)

## 🎯 Overview

Auth Service handles all authentication and authorization operations for the SmartConnect platform, including:
- User registration and login
- JWT token generation and validation
- Refresh token rotation
- Password reset
- Account management

## ✨ Features

- ✅ JWT-based authentication
- ✅ Refresh token rotation
- ✅ Role-based access control (RBAC)
- ✅ Password reset via email
- ✅ Account locking after failed attempts
- ✅ Redis caching for tokens
- ✅ API documentation with Swagger
- ✅ Database migrations with Flyway
- ✅ Comprehensive error handling
- ✅ Health checks and metrics

## 🛠 Tech Stack

- **Framework:** Spring Boot 3.4.11
- **Language:** Java 21
- **Database:** PostgreSQL 14
- **Cache:** Redis 7
- **Security:** Spring Security + JWT
- **ORM:** Spring Data JPA + Hibernate
- **Migration:** Flyway
- **Build Tool:** Maven
- **API Docs:** SpringDoc OpenAPI
- **Containerization:** Docker

## 📁 Project Structure

```
smartconnect-auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/smartconnect/auth/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── model/               # JPA entities & enums
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── security/            # Security components
│   │   │   ├── exception/           # Exception handling
│   │   │   ├── util/                # Utility classes
│   │   │   └── mapper/              # Entity-DTO mappers
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/        # Flyway migrations
│   └── test/                        # Unit & integration tests
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Docker & Docker Compose** (optional)

## 🚀 Getting Started

### Option 1: Local Development (without Docker)

#### 1. Clone the repository

```bash
git clone https://github.com/smartconnect-edu/smartconnect-auth-service.git
cd smartconnect-auth-service
```

#### 2. Create environment file

```bash
cp .env.example .env
```

Edit `.env` with your configuration.

#### 3. Start PostgreSQL and Redis

```bash
# PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=smartconnect_auth \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:14

# Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

#### 4. Run the application

```bash
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run
./mvnw clean package -DskipTests
java -jar target/smartconnect-auth-service-0.0.1-SNAPSHOT.jar
```

The service will start on **http://localhost:3001**

### Option 2: Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f auth-service

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 📚 API Documentation

### Swagger UI

Once the application is running, access the API documentation:

**URL:** http://localhost:3001/api/swagger-ui.html

### Main Endpoints

#### Authentication

```http
POST /api/v1/auth/register          # Register new user
POST /api/v1/auth/login             # Login
POST /api/v1/auth/refresh-token     # Refresh access token
POST /api/v1/auth/logout            # Logout
POST /api/v1/auth/forgot-password   # Request password reset
POST /api/v1/auth/reset-password    # Reset password
GET  /api/v1/auth/me                # Get current user
```

#### Users

```http
GET    /api/v1/users                # Get all users
GET    /api/v1/users/{id}           # Get user by ID
PUT    /api/v1/users/{id}           # Update user
DELETE /api/v1/users/{id}           # Delete user
```

#### Health Check

```http
GET /api/health                     # Basic health check
GET /api/actuator/health            # Detailed health check
```

### Example Request

**Login:**

```bash
curl -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "admin",
    "password": "Admin@123456"
  }'
```

**Response:**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "admin",
      "email": "admin@smartconnect.edu.vn",
      "fullName": "System Administrator",
      "role": "SUPER_ADMIN"
    }
  }
}
```

## 🧪 Testing

### Run Tests

```bash
# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest=*Test

# Integration tests
./mvnw test -Dtest=*IntegrationTest

# With coverage
./mvnw test jacoco:report
```

### Test Credentials

Default users for testing:

| Username | Password | Role |
|----------|----------|------|
| admin | Admin@123456 | SUPER_ADMIN |
| student_demo | Student@123 | STUDENT |
| teacher_demo | Teacher@123 | TEACHER |

## 🔒 Security

### Important Security Guidelines

⚠️ **NEVER commit sensitive files to version control!**

See [SECURITY.md](SECURITY.md) for detailed security guidelines.

### Quick Security Checklist

- ✅ Use `.env.example` as template (never commit `.env`)
- ✅ Generate strong JWT secret: `openssl rand -base64 64`
- ✅ Use different credentials for each environment
- ✅ Enable HTTPS in production
- ✅ Rotate secrets regularly
- ✅ Review `.gitignore` before committing

### Protected Files

These files are **automatically ignored** by Git:
- `.env` and `.env.*` (except `.env.example`)
- `application-prod.properties`
- `*-secret.properties`
- `*.properties.local`

### Setup Instructions

1. Copy environment template:
   ```bash
   cp .env.example .env
   ```

2. Generate secure JWT secret:
   ```bash
   openssl rand -base64 64
   ```

3. Update `.env` with your configuration

4. Verify `.env` is NOT tracked:
   ```bash
   git status
   # .env should NOT appear in output
   ```

For complete security guidelines, see [SECURITY.md](SECURITY.md).

## 🗄️ Database

### Migrations

Flyway migrations are in `src/main/resources/db/migration/`

```bash
# Apply migrations
./mvnw flyway:migrate

# Validate migrations
./mvnw flyway:validate

# Clean database (⚠️ drops all data)
./mvnw flyway:clean
```

### Schema

Main tables:
- `users` - User accounts
- `refresh_tokens` - JWT refresh tokens

## 🔧 Configuration

### Environment Variables

Key environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | smartconnect_auth |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT secret key | (required) |
| `SERVER_PORT` | Service port | 3001 |

### Application Profiles

- **dev** - Development mode (detailed logging, Swagger enabled)
- **prod** - Production mode (minimal logging, Swagger disabled)
- **test** - Testing mode (H2 in-memory database)

## 📦 Build & Deployment

### Build JAR

```bash
./mvnw clean package -DskipTests
```

Output: `target/smartconnect-auth-service-0.0.1-SNAPSHOT.jar`

### Build Docker Image

```bash
docker build -t smartconnect/auth-service:latest .
```

### Run Docker Container

```bash
docker run -d \
  --name auth-service \
  -p 3001:3001 \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  -e JWT_SECRET=your-secret \
  smartconnect/auth-service:latest
```

## 🔍 Monitoring

### Health Checks

```bash
# Basic health
curl http://localhost:3001/api/health

# Actuator health
curl http://localhost:3001/api/actuator/health

# Metrics
curl http://localhost:3001/api/actuator/metrics
```

### Logs

```bash
# View logs
docker-compose logs -f auth-service

# Location: logs/auth-service.log
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

MIT License - see LICENSE file for details

## 👥 Authors

SmartConnect Development Team

## 📧 Contact

- Email: dev@smartconnect.edu.vn
- GitHub: https://github.com/smartconnect-edu

---

**Version:** 1.0.0  
**Last Updated:** November 2025

