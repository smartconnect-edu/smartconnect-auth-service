# SmartConnect Auth Service - Railway Deploy Script (PowerShell)
# Usage: .\deploy-railway.ps1

Write-Host "🚂 Railway Deployment Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if Railway CLI is installed
try {
    $railwayVersion = railway --version 2>&1
    Write-Host "✅ Railway CLI found: $railwayVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Railway CLI is not installed" -ForegroundColor Red
    Write-Host "📦 Install with: npm i -g @railway/cli" -ForegroundColor Yellow
    Write-Host "   Or visit: https://docs.railway.app/develop/cli" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check if logged in
Write-Host "🔐 Checking Railway login status..." -ForegroundColor Cyan
try {
    $whoami = railway whoami 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Not logged in"
    }
    Write-Host "✅ Logged in to Railway" -ForegroundColor Green
} catch {
    Write-Host "❌ Not logged in to Railway" -ForegroundColor Red
    Write-Host "🔑 Please login first:" -ForegroundColor Yellow
    railway login
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Login failed" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Check if project is linked
Write-Host "🔗 Checking project link..." -ForegroundColor Cyan
try {
    $status = railway status 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Not linked"
    }
    Write-Host "✅ Project linked" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Project not linked" -ForegroundColor Yellow
    Write-Host "🔗 Linking to Railway project..." -ForegroundColor Cyan
    railway link
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Failed to link project" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Show current status
Write-Host "📊 Current Railway Status:" -ForegroundColor Cyan
railway status
Write-Host ""

# Confirm deployment
$confirmation = Read-Host "🚀 Deploy to Railway? (y/n)"
if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
    Write-Host "❌ Deployment cancelled" -ForegroundColor Yellow
    exit 0
}

# Deploy
Write-Host ""
Write-Host "🚀 Deploying to Railway..." -ForegroundColor Cyan
railway up

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Deployment successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Viewing logs (Ctrl+C to exit)..." -ForegroundColor Cyan
    Start-Sleep -Seconds 2
    railway logs
} else {
    Write-Host "❌ Deployment failed" -ForegroundColor Red
    exit 1
}

