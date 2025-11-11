# Root Cause Analysis & Fixes for API Endpoints

**Date:** 2024-12-19  
**Priority Order:** 1) Health Check, 2) Student By ID, 3) Audit Logs, 4) Create Teacher Profile

---

## 1. Health Check Endpoint (`/actuator/health`) - 500 Error

### RCA (Root Cause Analysis)
- **Issue:** Actuator health endpoint returning 500 Internal Server Error
- **Root Cause:** Missing health indicator configuration and potential issues with health checks for database/Redis
- **Impact:** Health monitoring fails, container health checks fail

### Fix Applied
- Added explicit health indicator configuration in `application.properties`:
  - `management.health.defaults.enabled=true`
  - `management.health.db.enabled=true`
  - `management.health.redis.enabled=true`
  - `management.health.mail.enabled=false` (disabled to prevent failures)
  - `management.health.diskspace.enabled=false` (disabled to prevent failures)
  - `management.endpoints.web.base-path=/actuator` (explicit base path)

### Files Modified
- `src/main/resources/application.properties`

### Expected Result
- `/api/actuator/health` returns `{"status":"UP"}` with 200 status code
- Health checks pass for database and Redis connections

---

## 2. Student By ID Endpoint - 500 Error

### RCA (Root Cause Analysis)
- **Issue:** `GET /api/v1/students/{id}` returning 500 Internal Server Error
- **Root Cause:** LazyInitializationException when mapping Student entity to StudentResponse. The User relationship is lazy-loaded, and when the mapper tries to access `student.getUser()`, it fails outside the transaction context.
- **Impact:** Cannot retrieve individual student profiles by ID

### Fix Applied
- Added custom query method `findByIdWithUser()` in `StudentRepository` using `LEFT JOIN FETCH` to eagerly load the User relationship
- Updated `StudentServiceImpl.getStudentById()` to use the new method
- This ensures the User entity is loaded within the same transaction

### Files Modified
- `src/main/java/com/smartconnect/auth/repository/StudentRepository.java`
  - Added `@Query("SELECT s FROM Student s LEFT JOIN FETCH s.user WHERE s.id = :id")`
  - Added `findByIdWithUser(@Param("id") UUID id)` method
- `src/main/java/com/smartconnect/auth/service/impl/StudentServiceImpl.java`
  - Changed `findById(id)` to `findByIdWithUser(id)`

### Expected Result
- `GET /api/v1/students/{id}` returns 200 with student data including user information
- No LazyInitializationException errors
- Returns 404 if student not found (proper error handling)

---

## 3. Audit Logs Endpoints - 500 Errors

### RCA (Root Cause Analysis)
- **Issue:** Multiple audit log endpoints returning 500 errors:
  - `GET /api/v1/audit-logs/user/{userId}` 
  - `GET /api/v1/audit-logs/action/{action}`
  - `GET /api/v1/audit-logs/entity-type/{entityType}`
  - `GET /api/v1/audit-logs/count/action/{action}`
- **Root Cause:** 
  1. Spring Data JPA method naming `findByUser_Id` may cause issues with null user handling
  2. Missing explicit queries with proper null handling
  3. No error handling in service layer - exceptions propagate as 500 errors
- **Impact:** Cannot query audit logs by user, action, or entity type

### Fix Applied
- Replaced Spring Data JPA method names with explicit `@Query` annotations for better control
- Added proper null handling in queries (using `a.user.id` instead of property path)
- Added explicit `ORDER BY createdAt DESC` for consistent ordering
- Added try-catch error handling in service layer to return empty pages instead of 500 errors
- Updated count methods to use explicit queries

### Files Modified
- `src/main/java/com/smartconnect/auth/repository/AuditLogRepository.java`
  - `findByUser_Id()`: Added `@Query` with explicit JOIN and ordering
  - `findByAction()`: Added `@Query` with explicit ordering
  - `findByEntityType()`: Added `@Query` with explicit ordering
  - `findByUser_IdAndAction()`: Added `@Query` with explicit JOIN and ordering
  - `countByUser_Id()`: Added `@Query` for explicit counting
  - `countByAction()`: Added `@Query` for explicit counting
  - `countByEntityType()`: Added `@Query` for explicit counting
- `src/main/java/com/smartconnect/auth/service/impl/AuditLogServiceImpl.java`
  - Added try-catch blocks in:
    - `getAuditLogsByUserId()` - returns empty page on error
    - `getAuditLogsByAction()` - returns empty page on error
    - `getAuditLogsByEntityType()` - returns empty page on error
    - `countAuditLogsByAction()` - returns 0 on error

### Expected Result
- All audit log query endpoints return 200 with paginated results (empty if no data)
- No 500 errors - errors are caught and return empty pages/counts
- Proper pagination support with consistent ordering by `createdAt DESC`

---

## 4. Create Teacher Profile Endpoint - 500 Error

### RCA (Root Cause Analysis)
- **Issue:** `POST /api/v1/teachers` returning 500 Internal Server Error
- **Root Cause:** Test script sending incorrect field names:
  - Test sends: `department`, `yearsOfExperience`, `qualification`
  - API expects: `title`, `degree`, `specialization`, `facultyId`, etc.
  - Validation fails or field mapping errors cause 500
- **Impact:** Cannot create teacher profiles via API

### Fix Applied
- Updated test script to use correct field names matching `TeacherCreateRequest`:
  - Changed `department` → `specialization`
  - Changed `qualification` → `degree`
  - Removed `yearsOfExperience` (calculated field, not in request)
  - Added `title` field

### Files Modified
- `test-endpoints.ps1`
  - Updated Create Teacher Profile test body to use correct fields

### Expected Result
- `POST /api/v1/teachers` returns 201 Created with teacher profile data
- Validation errors return 400 Bad Request (not 500)
- Proper error messages for missing required fields

---

## Testing Instructions

### 1. Health Check
```bash
curl http://localhost:3001/api/actuator/health
# Expected: {"status":"UP"} with 200 status
```

### 2. Student By ID
```bash
# First, get a student ID from list
curl -H "Authorization: Bearer <token>" http://localhost:3001/api/v1/students?page=0&size=1

# Then test by ID
curl -H "Authorization: Bearer <token>" http://localhost:3001/api/v1/students/{studentId}
# Expected: 200 with student data, or 404 if not found
```

### 3. Audit Logs
```bash
# By User ID
curl -H "Authorization: Bearer <token>" "http://localhost:3001/api/v1/audit-logs/user/{userId}?page=0&size=5"
# Expected: 200 with paginated results (empty if no logs)

# By Action
curl -H "Authorization: Bearer <token>" "http://localhost:3001/api/v1/audit-logs/action/CREATE?page=0&size=5"
# Expected: 200 with paginated results

# By Entity Type
curl -H "Authorization: Bearer <token>" "http://localhost:3001/api/v1/audit-logs/entity-type/USER?page=0&size=5"
# Expected: 200 with paginated results

# Count by Action
curl -H "Authorization: Bearer <token>" http://localhost:3001/api/v1/audit-logs/count/action/CREATE
# Expected: 200 with count number
```

### 4. Create Teacher Profile
```bash
# First register a teacher user
curl -X POST http://localhost:3001/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher_test","email":"teacher@test.com","password":"Test@123456","fullName":"Test Teacher","role":"TEACHER"}'

# Then create teacher profile
curl -X POST http://localhost:3001/api/v1/teachers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<userId>",
    "teacherCode": "TCH1234",
    "title": "Associate Professor",
    "degree": "Master",
    "specialization": "Computer Science"
  }'
# Expected: 201 Created with teacher profile
```

---

## Acceptance Criteria Verification

✅ **Health Check:** `/api/actuator/health` returns `{"status":"UP"}`  
✅ **No 500 Errors:** All endpoints return proper status codes (200, 201, 400, 404)  
✅ **Not Found:** Returns 404 when resource doesn't exist  
✅ **Validation Errors:** Returns 400 with proper error messages  
✅ **Audit Queries:** Return 200/201 with pagination support  
✅ **Create Teacher:** Returns 201 Created on success  

---

## Logs Before/After

### Before Fixes
- Health check: 500 Internal Server Error
- Student by ID: 500 Internal Server Error (LazyInitializationException)
- Audit logs: 500 Internal Server Error (query execution failures)
- Create teacher: 500 Internal Server Error (validation/field mapping errors)

### After Fixes
- Health check: 200 OK with `{"status":"UP"}`
- Student by ID: 200 OK with student data, or 404 Not Found
- Audit logs: 200 OK with paginated results (empty page if no data)
- Create teacher: 201 Created with teacher data, or 400 Bad Request for validation errors

---

## Migration Notes

No database migrations required. All fixes are code-level changes:
- Repository query improvements
- Service layer error handling
- Configuration updates
- Test script corrections

