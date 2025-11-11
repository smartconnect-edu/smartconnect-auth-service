-- =====================================================
-- Migration V5: Create ADMINS table
-- Description: Admin profile information extending USERS
-- =====================================================

-- Create admins table
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    admin_code VARCHAR(20) UNIQUE NOT NULL,
    department VARCHAR(100),  -- Department/Division
    position VARCHAR(100),  -- Job position/title
    permissions JSONB DEFAULT '[]'::jsonb,  -- Granular permissions
    access_level INTEGER DEFAULT 1,  -- 1=basic, 2=advanced, 3=super
    can_manage_users BOOLEAN DEFAULT FALSE,
    can_manage_courses BOOLEAN DEFAULT FALSE,
    can_manage_grades BOOLEAN DEFAULT FALSE,
    can_view_reports BOOLEAN DEFAULT FALSE,
    can_manage_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    hire_date DATE,
    notes TEXT,  -- Internal notes
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to users table
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT check_access_level CHECK (access_level >= 1 AND access_level <= 3),
    CONSTRAINT check_hire_date CHECK (hire_date <= CURRENT_DATE)
);

-- Create indexes for performance
CREATE INDEX idx_admins_user_id ON admins(user_id);
CREATE INDEX idx_admins_admin_code ON admins(admin_code);
CREATE INDEX idx_admins_department ON admins(department);
CREATE INDEX idx_admins_is_active ON admins(is_active);
CREATE INDEX idx_admins_access_level ON admins(access_level);
CREATE INDEX idx_admins_created_at ON admins(created_at);

-- Create GIN index for JSONB permissions
CREATE INDEX idx_admins_permissions ON admins USING GIN (permissions);

-- Create composite index for common queries
CREATE INDEX idx_admins_department_active ON admins(department, is_active);

-- Add table and column comments
COMMENT ON TABLE admins IS 'Admin profile information extending users table';
COMMENT ON COLUMN admins.admin_code IS 'Unique admin identification code (e.g., AD2024001)';
COMMENT ON COLUMN admins.department IS 'Department or division the admin belongs to';
COMMENT ON COLUMN admins.permissions IS 'Granular permissions in JSON format for fine-grained access control';
COMMENT ON COLUMN admins.access_level IS 'Overall access level: 1=Basic Admin, 2=Advanced Admin, 3=Super Admin';
COMMENT ON COLUMN admins.can_manage_users IS 'Permission to create/update/delete users';
COMMENT ON COLUMN admins.can_manage_courses IS 'Permission to manage courses and classes';
COMMENT ON COLUMN admins.can_manage_grades IS 'Permission to view and modify grades';
COMMENT ON COLUMN admins.can_view_reports IS 'Permission to access system reports and analytics';
COMMENT ON COLUMN admins.can_manage_system IS 'Permission to manage system settings and configurations';

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_admins_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_admins_updated_at
    BEFORE UPDATE ON admins
    FOR EACH ROW
    EXECUTE FUNCTION update_admins_updated_at();

-- Insert default super admin permissions template (for reference)
COMMENT ON COLUMN admins.permissions IS 'Example permissions JSON:
{
  "users": ["create", "read", "update", "delete"],
  "courses": ["create", "read", "update", "delete"],
  "grades": ["read", "update"],
  "reports": ["read", "export"],
  "system": ["configure", "backup", "restore"]
}';

