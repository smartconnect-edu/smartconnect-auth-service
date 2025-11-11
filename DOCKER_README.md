# Docker Setup Guide

Hướng dẫn build và chạy SmartConnect Auth Service với Docker.

## Yêu cầu

- Docker Desktop (Windows/Mac) hoặc Docker Engine (Linux)
- Docker Compose v2.0+

## Cấu hình nhanh

### 1. Tạo file .env

```bash
# Windows PowerShell
Copy-Item env.example .env

# Linux/Mac
cp env.example .env
```

Sau đó chỉnh sửa file `.env` với các giá trị phù hợp (đặc biệt là `DB_PASSWORD` và `JWT_SECRET`).

### 2. Build và chạy

**Windows (PowerShell):**
```powershell
# Chạy với cấu hình mặc định
.\docker-build.ps1 up

# Build lại image và chạy
.\docker-build.ps1 up -Build

# Chỉ build image
.\docker-build.ps1 build

# Xem logs
.\docker-build.ps1 logs

# Dừng containers
.\docker-build.ps1 down

# Dọn dẹp (xóa volumes và containers)
.\docker-build.ps1 clean
```

**Linux/Mac (Bash):**
```bash
# Cho phép thực thi script
chmod +x docker-build.sh

# Chạy với cấu hình mặc định
./docker-build.sh up

# Build lại image và chạy
./docker-build.sh up --build

# Chỉ build image
./docker-build.sh build

# Xem logs
./docker-build.sh logs

# Dừng containers
./docker-build.sh down

# Dọn dẹp (xóa volumes và containers)
./docker-build.sh clean
```

**Hoặc sử dụng docker-compose trực tiếp:**
```bash
# Build và chạy
docker-compose up -d --build

# Xem logs
docker-compose logs -f

# Dừng
docker-compose down

# Dừng và xóa volumes
docker-compose down -v
```

## Services

Sau khi chạy thành công, các services sẽ có sẵn tại:

- **Auth Service API**: http://localhost:3001/api
- **Swagger UI**: http://localhost:3001/api/swagger-ui.html
- **Health Check**: http://localhost:3001/api/actuator/health
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## Cấu trúc Docker

### Containers

1. **smartconnect-postgres**: PostgreSQL database
2. **smartconnect-redis**: Redis cache
3. **smartconnect-auth-service**: Spring Boot application

### Volumes

- `postgres_data`: Lưu trữ dữ liệu PostgreSQL
- `redis_data`: Lưu trữ dữ liệu Redis
- `app_logs`: Lưu trữ logs của application

### Networks

- `smartconnect-network`: Bridge network cho các containers

## Environment Variables

Các biến môi trường quan trọng trong file `.env`:

```env
# Database
DB_PASSWORD=your-secure-password

# JWT (phải là Base64 encoded, ít nhất 64 bytes)
JWT_SECRET=your-base64-encoded-secret-key

# Redis (optional)
REDIS_PASSWORD=

# Mail (optional)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## Troubleshooting

### Container không start được

1. Kiểm tra logs:
   ```bash
   docker-compose logs auth-service
   ```

2. Kiểm tra health status:
   ```bash
   docker-compose ps
   ```

3. Kiểm tra ports có bị chiếm không:
   ```bash
   # Windows
   netstat -ano | findstr :3001
   netstat -ano | findstr :5432
   netstat -ano | findstr :6379
   
   # Linux/Mac
   lsof -i :3001
   lsof -i :5432
   lsof -i :6379
   ```

### Database connection errors

1. Đảm bảo PostgreSQL container đã healthy:
   ```bash
   docker-compose ps postgres
   ```

2. Kiểm tra password trong `.env` khớp với docker-compose.yml

3. Thử restart containers:
   ```bash
   docker-compose restart
   ```

### Build errors

1. Xóa cache và build lại:
   ```bash
   docker-compose build --no-cache
   ```

2. Kiểm tra Docker có đủ disk space:
   ```bash
   docker system df
   ```

### Xóa tất cả và bắt đầu lại

```bash
# Dừng và xóa tất cả
docker-compose down -v

# Xóa images
docker rmi smartconnect-auth-service_auth-service

# Xóa volumes
docker volume prune

# Build và chạy lại
docker-compose up -d --build
```

## Production Notes

⚠️ **Quan trọng cho production:**

1. Thay đổi tất cả passwords mặc định
2. Sử dụng JWT secret mạnh (ít nhất 512-bit, Base64 encoded)
3. Cấu hình CORS phù hợp với domain của bạn
4. Sử dụng secrets management (Docker secrets, AWS Secrets Manager, etc.)
5. Cấu hình backup cho PostgreSQL
6. Sử dụng reverse proxy (nginx) với SSL/TLS
7. Giới hạn resources cho containers
8. Bật logging và monitoring

## Health Checks

Tất cả containers đều có health checks:

- **PostgreSQL**: `pg_isready -U postgres`
- **Redis**: `redis-cli ping`
- **Auth Service**: `http://localhost:3001/api/actuator/health`

Kiểm tra health status:
```bash
docker-compose ps
```

## Generate Sample Data

Sau khi service đã chạy, bạn có thể generate sample data:

```bash
# Generate với default values (5 admins, 10 teachers, 20 students)
curl -X POST http://localhost:3001/api/v1/dev/generate-sample-data

# Generate với custom counts
curl -X POST "http://localhost:3001/api/v1/dev/generate-sample-data?adminCount=10&teacherCount=50&studentCount=100"
```

Tất cả users được tạo sẽ có password: `Admin@123`

