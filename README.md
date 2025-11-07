# 🔐 SmartConnect Authentication Service

Microservice xác thực và phân quyền cho nền tảng SmartConnect, được xây dựng với Spring Boot 3, JWT, Redis và PostgreSQL.

---

## ✨ Tính năng chính

### 🔒 Authentication & Authorization
- ✅ **JWT-based Authentication** với Access & Refresh Token
- ✅ **Role-based Access Control** (RBAC)
- ✅ **Account Lock Protection** sau nhiều lần đăng nhập sai
- ✅ **Token Blacklist** cho logout an toàn
- ✅ **Password Encryption** với BCrypt

### 🚀 API Features
- ✅ RESTful API với OpenAPI/Swagger documentation
- ✅ Rate limiting với Redis
- ✅ CORS configuration
- ✅ Actuator health checks
- ✅ Comprehensive error handling

### 💾 Database
- ✅ **PostgreSQL** với Flyway migration
- ✅ **Redis** cho caching và session management
- ✅ Optimized queries với JPA

### 🧪 Testing
- ✅ Unit tests với JUnit 5
- ✅ Integration tests
- ✅ Security tests
- ✅ Test coverage > 80%

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.4.11 |
| Language | Java 21 |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Database | PostgreSQL + Redis |
| Migration | Flyway |
| Documentation | SpringDoc OpenAPI 3 |
| Build Tool | Maven |
| Container | Docker |

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### 1. Clone & Setup

```bash
git clone https://github.com/yourusername/smartconnect-auth-service.git
cd smartconnect-auth-service
```

### 2. Start Dependencies

```bash
docker-compose up -d postgres redis
```

### 3. Configure Environment

```bash
cp env.example .env
# Edit .env with your settings
```

### 4. Run Application

```bash
./mvnw spring-boot:run
```

### 5. Access

- **API Base URL:** http://localhost:3001/api
- **Swagger UI:** http://localhost:3001/api/swagger-ui.html
- **Health Check:** http://localhost:3001/api/actuator/health

---

## 📦 Deploy to Railway

### Quick Deploy (5 minutes)

1. **Push to GitHub:**
```bash
git push origin main
```

2. **Deploy to Railway:**
   - Visit [railway.app/new](https://railway.app/new)
   - Select your repository
   - Add PostgreSQL and Redis databases
   - Configure environment variables

📖 **Detailed guide:** [QUICK_START_RAILWAY.md](./QUICK_START_RAILWAY.md)

### Using Deploy Script

```bash
# Install Railway CLI
npm i -g @railway/cli

# Deploy (Windows)
.\deploy-railway.ps1

# Deploy (Linux/Mac)
./deploy-railway.sh
```

📖 **Full documentation:** [RAILWAY_DEPLOY.md](./RAILWAY_DEPLOY.md)

---

## 🔐 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | User login | ❌ |
| POST | `/api/auth/refresh` | Refresh access token | ❌ |
| POST | `/api/auth/logout` | User logout | ✅ |
| GET | `/api/auth/me` | Get current user | ✅ |

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/{id}` | Get user by ID | ✅ |
| GET | `/api/users/username/{username}` | Get user by username | ✅ |
| GET | `/api/users` | List all users (admin) | ✅ Admin |

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw clean test jacoco:report

# Run specific test
./mvnw test -Dtest=AuthServiceTest
```

View coverage report: `target/site/jacoco/index.html`

---

## 📁 Project Structure

```
smartconnect-auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/smartconnect/auth/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── filter/          # Security filters
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── scheduler/       # Scheduled tasks
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utilities
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Test files
├── docker-compose.yml           # Local development
├── Dockerfile                   # Production build
├── railway.toml                 # Railway config
└── pom.xml                      # Maven dependencies
```

---

## 🔧 Configuration

### Environment Variables

Key environment variables (see `env.example`):

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=smartconnect_auth
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-here
JWT_ACCESS_TOKEN_EXPIRATION=86400000   # 24 hours
JWT_REFRESH_TOKEN_EXPIRATION=604800000 # 7 days

# Security
ACCOUNT_LOCK_THRESHOLD=5
ACCOUNT_LOCK_DURATION_MINUTES=30
```

---

## 🔒 Security Features

### 1. Password Security
- BCrypt hashing with strength 12
- Password validation rules
- Secure password reset flow

### 2. Account Protection
- Auto-lock after 5 failed attempts
- 30-minute lock duration
- Auto-unlock after timeout

### 3. Token Security
- JWT with HS512 algorithm
- Access token: 24 hours
- Refresh token: 7 days
- Token blacklist on logout

### 4. API Security
- CORS configuration
- Rate limiting
- Input validation
- SQL injection prevention

---

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:3001/api/actuator/health

# Detailed health
curl http://localhost:3001/api/actuator/health/details
```

### Logs

```bash
# View logs
tail -f logs/auth-service.log

# Docker logs
docker logs -f smartconnect-auth-service
```

---

## 🐛 Troubleshooting

### Common Issues

**1. Database connection failed**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -p 5432 -U postgres -d smartconnect_auth
```

**2. Redis connection failed**
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli -h localhost -p 6379 ping
```

**3. Port already in use**
```bash
# Find process using port 3001
netstat -ano | findstr :3001

# Kill process (Windows)
taskkill /PID <PID> /F
```

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License.

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/yourusername/smartconnect-auth-service/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/smartconnect-auth-service/discussions)

---

## 🎉 What's Next?

- [ ] Email verification
- [ ] Password reset via email
- [ ] OAuth2 integration (Google, GitHub)
- [ ] Two-factor authentication (2FA)
- [ ] User profile management
- [ ] Audit logs
- [ ] Advanced rate limiting

---

**Made with ❤️ by SmartConnect Team**

