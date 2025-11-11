# PowerShell script to build and run SmartConnect Auth Service with Docker

param(
    [string]$Action = "up",
    [switch]$Build = $false,
    [switch]$Down = $false,
    [switch]$Logs = $false,
    [switch]$Clean = $false
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SmartConnect Auth Service - Docker" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Check if .env file exists
if (-not (Test-Path ".env")) {
    Write-Host "⚠️  .env file not found!" -ForegroundColor Yellow
    Write-Host "📋 Creating .env from env.example..." -ForegroundColor Yellow
    Copy-Item "env.example" ".env"
    Write-Host "✅ .env file created. Please update it with your configuration." -ForegroundColor Green
    Write-Host ""
}

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}

# Actions
switch ($Action.ToLower()) {
    "build" {
        Write-Host "🔨 Building Docker image..." -ForegroundColor Blue
        docker-compose build --no-cache
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Build completed successfully!" -ForegroundColor Green
        } else {
            Write-Host "❌ Build failed!" -ForegroundColor Red
            exit 1
        }
    }
    "up" {
        if ($Build) {
            Write-Host "🔨 Building and starting containers..." -ForegroundColor Blue
            docker-compose up -d --build
        } else {
            Write-Host "🚀 Starting containers..." -ForegroundColor Blue
            docker-compose up -d
        }
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Containers started successfully!" -ForegroundColor Green
            Write-Host ""
            Write-Host "📊 Container Status:" -ForegroundColor Cyan
            docker-compose ps
            Write-Host ""
            Write-Host "🌐 Services:" -ForegroundColor Cyan
            Write-Host "   - Auth Service: http://localhost:3001/api" -ForegroundColor White
            Write-Host "   - Swagger UI: http://localhost:3001/api/swagger-ui.html" -ForegroundColor White
            Write-Host "   - Health Check: http://localhost:3001/api/actuator/health" -ForegroundColor White
            Write-Host "   - PostgreSQL: localhost:5432" -ForegroundColor White
            Write-Host "   - Redis: localhost:6379" -ForegroundColor White
            Write-Host ""
            Write-Host "📝 View logs: .\docker-build.ps1 -Logs" -ForegroundColor Yellow
            Write-Host "🛑 Stop services: .\docker-build.ps1 -Down" -ForegroundColor Yellow
        } else {
            Write-Host "❌ Failed to start containers!" -ForegroundColor Red
            exit 1
        }
    }
    "down" {
        Write-Host "🛑 Stopping containers..." -ForegroundColor Blue
        docker-compose down
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Containers stopped!" -ForegroundColor Green
        }
    }
    "logs" {
        Write-Host "📋 Showing logs (Press Ctrl+C to exit)..." -ForegroundColor Blue
        docker-compose logs -f
    }
    "clean" {
        Write-Host "🧹 Cleaning up Docker resources..." -ForegroundColor Blue
        docker-compose down -v --remove-orphans
        docker system prune -f
        Write-Host "✅ Cleanup completed!" -ForegroundColor Green
    }
    default {
        Write-Host "Usage: .\docker-build.ps1 [Action] [Options]" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Actions:" -ForegroundColor Cyan
        Write-Host "  up      - Start containers (default)" -ForegroundColor White
        Write-Host "  build   - Build Docker image" -ForegroundColor White
        Write-Host "  down    - Stop containers" -ForegroundColor White
        Write-Host "  logs    - Show logs" -ForegroundColor White
        Write-Host "  clean   - Clean up containers and volumes" -ForegroundColor White
        Write-Host ""
        Write-Host "Options:" -ForegroundColor Cyan
        Write-Host "  -Build  - Rebuild images before starting" -ForegroundColor White
        Write-Host ""
        Write-Host "Examples:" -ForegroundColor Cyan
        Write-Host "  .\docker-build.ps1 up" -ForegroundColor White
        Write-Host "  .\docker-build.ps1 up -Build" -ForegroundColor White
        Write-Host "  .\docker-build.ps1 logs" -ForegroundColor White
        Write-Host "  .\docker-build.ps1 down" -ForegroundColor White
        Write-Host "  .\docker-build.ps1 clean" -ForegroundColor White
    }
}

