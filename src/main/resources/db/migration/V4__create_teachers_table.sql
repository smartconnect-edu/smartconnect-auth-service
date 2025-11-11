-- =====================================================
-- Migration V4: Create TEACHERS table
-- Description: Teacher profile information extending USERS
-- =====================================================

-- Create teachers table
CREATE TABLE IF NOT EXISTS teachers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    teacher_code VARCHAR(20) UNIQUE NOT NULL,
    faculty_id UUID,  -- Will be FK when FACULTIES table exists
    title VARCHAR(50),  -- Academic title (e.g., Professor, Associate Professor)
    degree VARCHAR(50),  -- Academic degree (e.g., PhD, Master)
    specialization TEXT,  -- Area of expertise
    office VARCHAR(50),  -- Office location
    office_hours VARCHAR(200),  -- Office hours schedule
    bio TEXT,  -- Short biography
    research_interests TEXT,  -- Research interests
    publications_count INTEGER DEFAULT 0,
    hire_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to users table
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT check_publications_count CHECK (publications_count >= 0),
    CONSTRAINT check_hire_date CHECK (hire_date <= CURRENT_DATE)
);

-- Create indexes for performance
CREATE INDEX idx_teachers_user_id ON teachers(user_id);
CREATE INDEX idx_teachers_teacher_code ON teachers(teacher_code);
CREATE INDEX idx_teachers_faculty_id ON teachers(faculty_id);
CREATE INDEX idx_teachers_is_active ON teachers(is_active);
CREATE INDEX idx_teachers_hire_date ON teachers(hire_date);
CREATE INDEX idx_teachers_created_at ON teachers(created_at);

-- Create composite index for common queries
CREATE INDEX idx_teachers_faculty_active ON teachers(faculty_id, is_active);

-- Add table and column comments
COMMENT ON TABLE teachers IS 'Teacher profile information extending users table';
COMMENT ON COLUMN teachers.teacher_code IS 'Unique teacher identification code (e.g., GV2024001)';
COMMENT ON COLUMN teachers.faculty_id IS 'Reference to faculty/department (will be FK when faculties table exists)';
COMMENT ON COLUMN teachers.title IS 'Academic title (Professor, Associate Professor, Assistant Professor, Lecturer)';
COMMENT ON COLUMN teachers.degree IS 'Highest academic degree (PhD, Master, Bachelor)';
COMMENT ON COLUMN teachers.specialization IS 'Primary area of teaching and research expertise';
COMMENT ON COLUMN teachers.office_hours IS 'Weekly office hours for student consultations';
COMMENT ON COLUMN teachers.publications_count IS 'Number of academic publications';

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_teachers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_teachers_updated_at
    BEFORE UPDATE ON teachers
    FOR EACH ROW
    EXECUTE FUNCTION update_teachers_updated_at();

