-- =====================================================
-- Migration V3: Create STUDENTS table
-- Description: Student profile information extending USERS
-- =====================================================

-- Create student_status enum
CREATE TYPE student_status AS ENUM ('ACTIVE', 'SUSPENDED', 'GRADUATED', 'DROPPED');

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    major_id UUID,  -- Will be FK when MAJORS table exists
    admission_year INTEGER NOT NULL,
    gpa DECIMAL(3,2) DEFAULT 0.00,
    status student_status DEFAULT 'ACTIVE',
    date_of_birth DATE,
    address TEXT,
    parent_phone VARCHAR(20),
    emergency_contact VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to users table
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT check_gpa CHECK (gpa >= 0.0 AND gpa <= 4.0),
    CONSTRAINT check_admission_year CHECK (admission_year >= 1900 AND admission_year <= 2100),
    CONSTRAINT check_date_of_birth CHECK (date_of_birth <= CURRENT_DATE)
);

-- Create indexes for performance
CREATE INDEX idx_students_user_id ON students(user_id);
CREATE INDEX idx_students_student_code ON students(student_code);
CREATE INDEX idx_students_major_id ON students(major_id);
CREATE INDEX idx_students_admission_year ON students(admission_year);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_created_at ON students(created_at);

-- Create composite index for common queries
CREATE INDEX idx_students_major_year ON students(major_id, admission_year);
CREATE INDEX idx_students_status_year ON students(status, admission_year);

-- Add table and column comments
COMMENT ON TABLE students IS 'Student profile information extending users table';
COMMENT ON COLUMN students.student_code IS 'Unique student identification code (e.g., SV2024001)';
COMMENT ON COLUMN students.major_id IS 'Reference to major/program (will be FK when majors table exists)';
COMMENT ON COLUMN students.gpa IS 'Grade Point Average (0.0 - 4.0 scale)';
COMMENT ON COLUMN students.status IS 'Current enrollment status';
COMMENT ON COLUMN students.emergency_contact IS 'Emergency contact information (name and phone)';

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_students_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_students_updated_at
    BEFORE UPDATE ON students
    FOR EACH ROW
    EXECUTE FUNCTION update_students_updated_at();

