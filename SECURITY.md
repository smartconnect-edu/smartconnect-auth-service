# Security Guidelines

## 🔒 Important Security Practices

### 1. Environment Variables
- **NEVER** commit `.env` files to version control
- Always use `.env.example` as a template
- Store sensitive credentials in environment variables
- Use different credentials for each environment (dev/staging/prod)

### 2. JWT Secret
Generate a strong JWT secret using:
```bash
openssl rand -base64 64
```

### 3. Database Credentials
- Use strong passwords for production databases
- Rotate credentials regularly
- Use least privilege principle for database users
- Never use default passwords

### 4. Files to NEVER Commit
- `.env` and `.env.*` files (except `.env.example`)
- `application-prod.properties`
- Any files containing actual passwords, API keys, or secrets
- Private keys or certificates

### 5. Production Deployment
- Use environment-specific configuration
- Enable HTTPS/TLS
- Use a secrets management system (AWS Secrets Manager, HashiCorp Vault, etc.)
- Enable security headers
- Configure proper CORS settings

### 6. Docker Security
- Never include secrets in Dockerfile
- Use Docker secrets or environment variables
- Scan Docker images for vulnerabilities
- Use multi-stage builds to reduce image size

### 7. Code Review Checklist
Before committing, ensure:
- [ ] No hardcoded passwords or API keys
- [ ] No `.env` files in commit
- [ ] No sensitive data in logs
- [ ] All secrets use environment variables
- [ ] `.gitignore` is properly configured

### 8. Reporting Security Issues
If you find a security vulnerability, please:
1. **DO NOT** open a public issue
2. Email security@smartconnect.com
3. Include detailed description and steps to reproduce

## 🔐 Environment Setup

### Development
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Update values in `.env` with your local configuration

3. Verify `.env` is in `.gitignore`

### Production
1. Never use example values in production
2. Use strong, randomly generated secrets
3. Store secrets in a secure vault
4. Use infrastructure-as-code for secret injection

## 📋 Security Audit Log
| Date | Action | Description |
|------|--------|-------------|
| 2025-11-03 | Initial Setup | Configured .gitignore and environment variables |

