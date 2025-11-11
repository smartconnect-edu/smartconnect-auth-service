#!/bin/bash
# Bash script to build and run SmartConnect Auth Service with Docker

set -e

ACTION="${1:-up}"
BUILD="${2:-}"

echo "========================================"
echo "SmartConnect Auth Service - Docker"
echo "========================================"
echo ""

# Change to script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "⚠️  .env file not found!"
    echo "📋 Creating .env from env.example..."
    cp env.example .env
    echo "✅ .env file created. Please update it with your configuration."
    echo ""
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker."
    exit 1
fi

# Actions
case "$ACTION" in
    build)
        echo "🔨 Building Docker image..."
        docker-compose build --no-cache
        if [ $? -eq 0 ]; then
            echo "✅ Build completed successfully!"
        else
            echo "❌ Build failed!"
            exit 1
        fi
        ;;
    up)
        if [ "$BUILD" = "--build" ]; then
            echo "🔨 Building and starting containers..."
            docker-compose up -d --build
        else
            echo "🚀 Starting containers..."
            docker-compose up -d
        fi
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ Containers started successfully!"
            echo ""
            echo "📊 Container Status:"
            docker-compose ps
            echo ""
            echo "🌐 Services:"
            echo "   - Auth Service: http://localhost:3001/api"
            echo "   - Swagger UI: http://localhost:3001/api/swagger-ui.html"
            echo "   - Health Check: http://localhost:3001/api/actuator/health"
            echo "   - PostgreSQL: localhost:5432"
            echo "   - Redis: localhost:6379"
            echo ""
            echo "📝 View logs: ./docker-build.sh logs"
            echo "🛑 Stop services: ./docker-build.sh down"
        else
            echo "❌ Failed to start containers!"
            exit 1
        fi
        ;;
    down)
        echo "🛑 Stopping containers..."
        docker-compose down
        if [ $? -eq 0 ]; then
            echo "✅ Containers stopped!"
        fi
        ;;
    logs)
        echo "📋 Showing logs (Press Ctrl+C to exit)..."
        docker-compose logs -f
        ;;
    clean)
        echo "🧹 Cleaning up Docker resources..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        echo "✅ Cleanup completed!"
        ;;
    *)
        echo "Usage: ./docker-build.sh [Action] [Options]"
        echo ""
        echo "Actions:"
        echo "  up      - Start containers (default)"
        echo "  build   - Build Docker image"
        echo "  down    - Stop containers"
        echo "  logs    - Show logs"
        echo "  clean   - Clean up containers and volumes"
        echo ""
        echo "Options:"
        echo "  --build - Rebuild images before starting"
        echo ""
        echo "Examples:"
        echo "  ./docker-build.sh up"
        echo "  ./docker-build.sh up --build"
        echo "  ./docker-build.sh logs"
        echo "  ./docker-build.sh down"
        echo "  ./docker-build.sh clean"
        ;;
esac

