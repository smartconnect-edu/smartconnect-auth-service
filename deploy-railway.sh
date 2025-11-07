#!/bin/bash

# SmartConnect Auth Service - Railway Deploy Script
# Usage: ./deploy-railway.sh

echo "🚂 Railway Deployment Script"
echo "================================"

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null
then
    echo "❌ Railway CLI is not installed"
    echo "📦 Install with: npm i -g @railway/cli"
    echo "   Or visit: https://docs.railway.app/develop/cli"
    exit 1
fi

echo "✅ Railway CLI found"
echo ""

# Check if logged in
echo "🔐 Checking Railway login status..."
if ! railway whoami &> /dev/null
then
    echo "❌ Not logged in to Railway"
    echo "🔑 Please login first:"
    railway login
    if [ $? -ne 0 ]; then
        echo "❌ Login failed"
        exit 1
    fi
fi

echo "✅ Logged in to Railway"
echo ""

# Check if project is linked
echo "🔗 Checking project link..."
if ! railway status &> /dev/null
then
    echo "⚠️  Project not linked"
    echo "🔗 Linking to Railway project..."
    railway link
    if [ $? -ne 0 ]; then
        echo "❌ Failed to link project"
        exit 1
    fi
fi

echo "✅ Project linked"
echo ""

# Show current status
echo "📊 Current Railway Status:"
railway status
echo ""

# Confirm deployment
read -p "🚀 Deploy to Railway? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "❌ Deployment cancelled"
    exit 0
fi

# Deploy
echo "🚀 Deploying to Railway..."
railway up

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Deployment successful!"
    echo ""
    echo "📝 Viewing logs (Ctrl+C to exit)..."
    sleep 2
    railway logs
else
    echo "❌ Deployment failed"
    exit 1
fi

