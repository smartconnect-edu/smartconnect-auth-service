-- =====================================================
-- Migration V7: Add is_deleted column to entity tables
-- Description: Add soft delete support via is_deleted column
-- =====================================================

-- Add is_deleted column to students table
ALTER TABLE students
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Add is_deleted column to teachers table
ALTER TABLE teachers
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Add is_deleted column to admins table
ALTER TABLE admins
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Add is_deleted column to refresh_tokens table
ALTER TABLE refresh_tokens
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Add is_deleted column to audit_logs table (if applicable)
ALTER TABLE audit_logs
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

-- Create indexes for soft delete queries
CREATE INDEX IF NOT EXISTS idx_students_is_deleted ON students(is_deleted);
CREATE INDEX IF NOT EXISTS idx_teachers_is_deleted ON teachers(is_deleted);
CREATE INDEX IF NOT EXISTS idx_admins_is_deleted ON admins(is_deleted);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_is_deleted ON refresh_tokens(is_deleted);
CREATE INDEX IF NOT EXISTS idx_audit_logs_is_deleted ON audit_logs(is_deleted);

-- Add comments
COMMENT ON COLUMN students.is_deleted IS 'Soft delete flag - TRUE if record is deleted';
COMMENT ON COLUMN teachers.is_deleted IS 'Soft delete flag - TRUE if record is deleted';
COMMENT ON COLUMN admins.is_deleted IS 'Soft delete flag - TRUE if record is deleted';
COMMENT ON COLUMN refresh_tokens.is_deleted IS 'Soft delete flag - TRUE if record is deleted';
COMMENT ON COLUMN audit_logs.is_deleted IS 'Soft delete flag - TRUE if record is deleted';

