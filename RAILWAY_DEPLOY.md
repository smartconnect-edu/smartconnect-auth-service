# 🚂 Railway Deployment Guide - SmartConnect Auth Service

## 📋 Tổng quan

Hướng dẫn deploy **SmartConnect Auth Service** lên Railway.app với **PostgreSQL + Redis** hoàn toàn miễn phí ($5 credit/tháng).

**Chi phí ước tính:** ~$3-4/tháng (trong $5 credit miễn phí)

---

## 🎯 Bước 1: Chuẩn bị

### 1.1. Tạo tài khoản Railway
1. Truy cập [railway.app](https://railway.app)
2. Đăng nhập bằng GitHub
3. Verify email (nếu cần)

### 1.2. Cài đặt Railway CLI (Tùy chọn)
```bash
# Windows (PowerShell)
iwr https://railway.app/install.ps1 | iex

# Mac/Linux
curl -fsSL https://railway.app/install.sh | sh

# Hoặc dùng npm
npm i -g @railway/cli
```

### 1.3. Login Railway CLI
```bash
railway login
```

---

## 🚀 Bước 2: Deploy bằng Railway Dashboard (Dễ nhất)

### 2.1. Push code lên GitHub
```bash
cd smartconnect-auth-service

# Init git (nếu chưa có)
git init
git add .
git commit -m "Initial commit"

# Push to GitHub
git remote add origin https://github.com/YOUR_USERNAME/smartconnect-auth.git
git branch -M main
git push -u origin main
```

### 2.2. Tạo Project trên Railway

1. Vào [railway.app/new](https://railway.app/new)
2. Chọn **"Deploy from GitHub repo"**
3. Chọn repository `smartconnect-auth`
4. Railway sẽ tự động detect Dockerfile

### 2.3. Thêm PostgreSQL

1. Trong project, click **"+ New"** → **"Database"** → **"Add PostgreSQL"**
2. Railway tự động tạo database và set biến môi trường:
   - `DATABASE_URL` (format: postgresql://user:pass@host:port/db)
   - `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

### 2.4. Thêm Redis

1. Click **"+ New"** → **"Database"** → **"Add Redis"**
2. Railway tự động tạo Redis và set biến môi trường:
   - `REDIS_URL` (format: redis://default:pass@host:port)
   - `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`

---

## ⚙️ Bước 3: Cấu hình Environment Variables

Click vào service **smartconnect-auth-service** → tab **"Variables"** → Thêm các biến sau:

### 3.1. Spring Configuration
```bash
SPRING_PROFILES_ACTIVE=prod
```

### 3.2. Database (Railway tự động cung cấp)
Railway sẽ tự inject các biến:
- `DATABASE_URL` → parse thành `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

**Bạn cần map lại:**
```bash
# Nếu Railway cung cấp DATABASE_URL, add thêm:
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
```

### 3.3. Redis (Railway tự động cung cấp)
```bash
REDIS_HOST=${{Redis.REDIS_HOST}}
REDIS_PORT=${{Redis.REDIS_PORT}}
REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}
```

### 3.4. JWT Configuration (QUAN TRỌNG!)
```bash
# Generate strong secret: openssl rand -base64 64
JWT_SECRET=YOUR_SUPER_SECRET_KEY_AT_LEAST_512_BITS_CHANGE_THIS
JWT_ACCESS_TOKEN_EXPIRATION=86400000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

⚠️ **Generate JWT Secret:**
```bash
# Chạy lệnh này để tạo JWT secret ngẫu nhiên
openssl rand -base64 64
```

### 3.5. Security & CORS
```bash
ACCOUNT_LOCK_THRESHOLD=5
ACCOUNT_LOCK_DURATION_MINUTES=30

# Thay bằng domain frontend của bạn
CORS_ALLOWED_ORIGINS=https://your-frontend.railway.app,https://yourdomain.com
```

### 3.6. Server Configuration
```bash
SERVER_PORT=3001
SERVER_CONTEXT_PATH=/api
LOGGING_LEVEL=INFO
```

### 3.7. Mail (Optional - nếu dùng email features)
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

---

## 🔧 Bước 4: Deploy

### 4.1. Deploy tự động
Railway tự động deploy khi bạn push code lên GitHub.

### 4.2. Hoặc deploy thủ công với CLI
```bash
cd smartconnect-auth-service
railway link
railway up
```

### 4.3. Xem logs
```bash
railway logs
```

---

## 🌐 Bước 5: Expose Public Domain

1. Click vào service **smartconnect-auth-service**
2. Tab **"Settings"** → **"Networking"**
3. Click **"Generate Domain"**
4. Railway sẽ cấp domain: `smartconnect-auth-production.up.railway.app`

**API sẽ chạy tại:**
```
https://smartconnect-auth-production.up.railway.app/api
```

---

## 📝 Kiểm tra Deploy thành công

### Test Health Endpoint
```bash
curl https://your-app.railway.app/api/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Test Swagger UI
Truy cập:
```
https://your-app.railway.app/api/swagger-ui.html
```

---

## 🐛 Troubleshooting

### Issue 1: Build failed
**Error:** `Failed to build Dockerfile`

**Solution:**
- Check Dockerfile syntax
- Ensure `pom.xml` is correct
- Check Railway build logs

### Issue 2: App crashed after deploy
**Error:** `Application failed to start`

**Solution:**
```bash
# Check logs
railway logs

# Common issues:
# - Missing environment variables
# - Database connection failed
# - Redis connection failed
```

### Issue 3: Database connection timeout
**Error:** `Could not connect to PostgreSQL`

**Solution:**
- Ensure PostgreSQL service is running
- Check environment variables mapping
- Verify `DB_HOST`, `DB_PORT`, etc. are correctly set

### Issue 4: Redis connection failed
**Error:** `Cannot connect to Redis`

**Solution:**
- Ensure Redis service is running
- Check `REDIS_HOST`, `REDIS_PORT` are correctly set
- Redis password might be empty (check Railway dashboard)

---

## 💰 Cost Optimization

### Free Tier Limits
- **$5 credit/month** = ~500 hours uptime
- **Ước tính sử dụng:**
  - App: ~$2-3/month
  - PostgreSQL: ~$1/month
  - Redis: ~$0.5/month

### Tips giảm chi phí:
1. **Sleep không dùng:**
   - Railway không có auto-sleep (khác Render)
   - Nếu muốn save credit, pause services khi không dùng

2. **Monitor usage:**
   ```bash
   railway status
   ```

3. **Use smaller instances:**
   - Mặc định Railway tự scale
   - Có thể limit resources trong `railway.toml`

---

## 🔒 Security Checklist

- [ ] Đổi `JWT_SECRET` thành giá trị random mạnh
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Đổi password PostgreSQL (Railway auto-generate)
- [ ] Configure CORS với domain chính xác
- [ ] Không commit `.env` file
- [ ] Enable Railway's built-in security features

---

## 📚 Tài liệu tham khảo

- [Railway Docs](https://docs.railway.app)
- [Railway PostgreSQL](https://docs.railway.app/databases/postgresql)
- [Railway Redis](https://docs.railway.app/databases/redis)
- [Railway Environment Variables](https://docs.railway.app/develop/variables)

---

## 🎉 Next Steps

Sau khi deploy thành công:

1. **Test API endpoints** qua Swagger UI
2. **Connect frontend** với backend URL
3. **Setup monitoring** (Railway có built-in metrics)
4. **Configure custom domain** (nếu cần)
5. **Setup CI/CD** với GitHub Actions (optional)

---

## 💡 Quick Deploy Script (Railway CLI)

Tạo file `deploy.sh`:

```bash
#!/bin/bash

echo "🚂 Deploying to Railway..."

# Login (if needed)
railway login

# Link to project (first time only)
# railway link

# Deploy
railway up

# Show logs
railway logs

echo "✅ Deployment complete!"
echo "🌐 Visit: https://your-app.railway.app/api/swagger-ui.html"
```

Chạy:
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 📞 Support

Nếu gặp vấn đề:
1. Check Railway logs: `railway logs`
2. Railway Discord: [discord.gg/railway](https://discord.gg/railway)
3. GitHub Issues

**Happy Deploying! 🚀**

