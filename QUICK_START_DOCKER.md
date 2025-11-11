# Quick Start - Docker

## Bước 1: Tạo file .env

```powershell
# Windows
Copy-Item env.example .env

# Linux/Mac  
cp env.example .env
```

## Bước 2: Chạy với Docker

```powershell
# Windows PowerShell
.\docker-build.ps1 up -Build
```

```bash
# Linux/Mac
chmod +x docker-build.sh
./docker-build.sh up --build
```

## Bước 3: Kiểm tra services

- API: http://localhost:3001/api
- Swagger: http://localhost:3001/api/swagger-ui.html
- Health: http://localhost:3001/api/actuator/health

## Bước 4: Generate sample data (optional)

```bash
curl -X POST http://localhost:3001/api/v1/dev/generate-sample-data
```

## Các lệnh thường dùng

```powershell
# Xem logs
.\docker-build.ps1 logs

# Dừng
.\docker-build.ps1 down

# Dọn dẹp
.\docker-build.ps1 clean
```

Xem chi tiết tại [DOCKER_README.md](./DOCKER_README.md)

