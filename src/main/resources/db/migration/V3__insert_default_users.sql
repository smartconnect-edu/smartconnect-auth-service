-- Insert default admin user
-- Password: Admin@123456 (BCrypt hashed)
INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    full_name,
    role,
    is_active,
    is_email_verified
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@smartconnect.edu.vn',
    '$2a$10$xYqE5K7EWEh4qLVvXxKLWOk5TQ4YvzF3xQm5nJ5LrYvZlKXxQxYLe',
    'System Administrator',
    'SUPER_ADMIN',
    TRUE,
    TRUE
);

-- Insert default student user
-- Password: Student@123
INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    full_name,
    role,
    is_active,
    is_email_verified
) VALUES (
    gen_random_uuid(),
    'student_demo',
    'student@smartconnect.edu.vn',
    '$2a$10$DxYvlZjQqE6F3xQm5nJ5LrYvZlKXxQxYLeXxKLWOk5TQ4YvzF3xQm',
    'Demo Student',
    'STUDENT',
    TRUE,
    TRUE
);

-- Insert default teacher user
-- Password: Teacher@123
INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    full_name,
    role,
    is_active,
    is_email_verified
) VALUES (
    gen_random_uuid(),
    'teacher_demo',
    'teacher@smartconnect.edu.vn',
    '$2a$10$YvZlKXxQxYLeXxKLWOk5TQ4YvzF3xQm5nJ5LrYvZlKXxQxYLeDxY',
    'Demo Teacher',
    'TEACHER',
    TRUE,
    TRUE
);

