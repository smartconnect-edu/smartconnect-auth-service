# SmartConnect Auth Service - Setup Guide

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### 1. Clone Repository
```bash
git clone https://github.com/smartconnect-edu/smartconnect-auth-service.git
cd smartconnect-auth-service
```

### 2. Environment Configuration

#### Create .env file
```bash
cp .env.example .env
```

#### Update .env with your configuration
Edit `.env` file and replace:
- `DB_PASSWORD` - Your PostgreSQL password
- `JWT_SECRET` - Generate using: `openssl rand -base64 64`
- `MAIL_USERNAME` and `MAIL_PASSWORD` - Your email credentials

#### Generate JWT Secret
```bash
openssl rand -base64 64
```

### 3. Database Setup

#### Option A: Using Docker (Recommended)
```bash
docker-compose up -d postgres redis
```

#### Option B: Manual Installation
Install PostgreSQL and Redis, then create database:
```sql
CREATE DATABASE smartconnect_auth;
CREATE USER smartconnect WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE smartconnect_auth TO smartconnect;
```

### 4. Build & Run

#### Using Maven
```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

#### Using Docker
```bash
# Build image
docker build -t smartconnect-auth-service .

# Run container
docker-compose up -d
```

### 5. Verify Installation

#### Health Check
```bash
curl http://localhost:3001/api/actuator/health
```

#### API Documentation
Open: http://localhost:3001/api/swagger-ui.html

### 6. Run Tests
```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=AuthenticationServiceTest
```

## 📁 Project Structure
See [STRUCTURE_GUIDE.md](STRUCTURE_GUIDE.md) for detailed structure.

## 🔒 Security
See [SECURITY.md](SECURITY.md) for security guidelines.

## 🌍 Environment Profiles

### Development
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Testing
```bash
export SPRING_PROFILES_ACTIVE=test
mvn test
```

### Production
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/smartconnect-auth-service-1.0.0.jar
```

## 🔧 Common Issues

### Database Connection Failed
- Check PostgreSQL is running: `docker-compose ps`
- Verify credentials in `.env`
- Check port 5432 is not in use

### Redis Connection Failed
- Start Redis: `docker-compose up -d redis`
- Verify Redis is running: `redis-cli ping`

### JWT Token Invalid
- Ensure `JWT_SECRET` is properly set in `.env`
- Check token expiration settings

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - Logout

### Password Management
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password

See Swagger UI for complete API documentation.

## 🛠️ Development Tools

### Hot Reload
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

### Database Migrations
```bash
# Apply migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info
```

### Code Quality
```bash
# Run checkstyle
mvn checkstyle:check

# Run tests with coverage
mvn test jacoco:report
```

## 📞 Support
For issues or questions:
- Open an issue on GitHub
- Email: dev@smartconnect.com
- Documentation: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

