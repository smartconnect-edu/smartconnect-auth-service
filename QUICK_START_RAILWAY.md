# ⚡ Quick Start - Deploy to Railway in 5 Minutes

## 🎯 Chuẩn bị (1 phút)

1. **Đăng ký Railway:** [railway.app](https://railway.app) (dùng GitHub)
2. **Push code lên GitHub:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/YOUR_USERNAME/smartconnect-auth.git
   git push -u origin main
   ```

---

## 🚀 Deploy (3 phút)

### Bước 1: Tạo Project
1. Vào [railway.app/new](https://railway.app/new)
2. Chọn **"Deploy from GitHub repo"**
3. Chọn repository của bạn

### Bước 2: Thêm Database
1. Click **"+ New"** → **"Database"** → **"Add PostgreSQL"**
2. Click **"+ New"** → **"Database"** → **"Add Redis"**

### Bước 3: Config Environment Variables
Click vào **smartconnect-auth-service** → **Variables** → Add:

```bash
# Required
SPRING_PROFILES_ACTIVE=prod

# Database (auto-reference)
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

# Redis (auto-reference)
REDIS_HOST=${{Redis.REDIS_HOST}}
REDIS_PORT=${{Redis.REDIS_PORT}}
REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}

# JWT (Generate with: openssl rand -base64 64)
JWT_SECRET=YOUR_SUPER_SECRET_KEY_HERE_AT_LEAST_512_BITS

# CORS (your frontend URL)
CORS_ALLOWED_ORIGINS=https://your-frontend.railway.app
```

### Bước 4: Deploy!
Railway tự động deploy. Chờ 3-5 phút.

---

## 🌐 Get Public URL (1 phút)

1. Click service **smartconnect-auth-service**
2. **Settings** → **Networking** → **Generate Domain**
3. Copy URL: `https://smartconnect-auth-production.up.railway.app`

---

## ✅ Test API

```bash
# Health check
curl https://your-app.railway.app/api/actuator/health

# Swagger UI
open https://your-app.railway.app/api/swagger-ui.html
```

---

## 🐛 Nếu có lỗi

```bash
# Xem logs
railway logs

# Hoặc trong Railway Dashboard
Click service → Deployments → Latest → View logs
```

**Common issues:**
- ❌ Build failed → Check Dockerfile
- ❌ App crashed → Check environment variables
- ❌ DB connection failed → Verify `DB_*` variables

---

## 💰 Free Tier

- **$5 credit/month** (đủ chạy 24/7)
- **Chi phí thực tế:** ~$3-4/month
- **Database persistent** (không mất data)

---

## 📚 Chi tiết đầy đủ

Xem file: **[RAILWAY_DEPLOY.md](./RAILWAY_DEPLOY.md)**

---

## 🎉 Done!

API của bạn đã chạy tại:
```
https://your-app.railway.app/api
```

Swagger UI:
```
https://your-app.railway.app/api/swagger-ui.html
```

**Happy coding! 🚀**

