-- =====================================================
-- Migration V6: Create AUDIT_LOGS table
-- Description: Comprehensive audit logging for security and compliance
-- =====================================================

-- Create action_type enum
CREATE TYPE action_type AS ENUM (
    'CREATE', 'READ', 'UPDATE', 'DELETE',
    'LOGIN', 'LOGOUT', 'LOGIN_FAILED',
    'PASSWORD_CHANGE', 'PASSWORD_RESET',
    'PERMISSION_CHANGE', 'STATUS_CHANGE',
    'EXPORT', 'IMPORT', 'BACKUP', 'RESTORE'
);

-- Create entity_type enum
CREATE TYPE entity_type AS ENUM (
    'USER', 'STUDENT', 'TEACHER', 'ADMIN',
    'COURSE', 'CLASS', 'ENROLLMENT', 'GRADE',
    'DOCUMENT', 'NOTIFICATION', 'SYSTEM'
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,  -- Can be NULL for system actions
    action action_type NOT NULL,
    entity_type entity_type NOT NULL,
    entity_id UUID,  -- ID of the affected entity
    entity_name VARCHAR(255),  -- Human-readable name
    old_values JSONB,  -- Previous state (for UPDATE/DELETE)
    new_values JSONB,  -- New state (for CREATE/UPDATE)
    description TEXT,  -- Human-readable description
    ip_address VARCHAR(45),  -- IPv4 or IPv6
    user_agent TEXT,  -- Browser/client info
    request_method VARCHAR(10),  -- HTTP method (GET, POST, etc.)
    request_url TEXT,  -- Request endpoint
    status_code INTEGER,  -- HTTP status code
    error_message TEXT,  -- Error details if action failed
    session_id VARCHAR(255),  -- Session identifier
    duration_ms INTEGER,  -- Action duration in milliseconds
    metadata JSONB DEFAULT '{}'::jsonb,  -- Additional context
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to users table (nullable for system actions)
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance and querying
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);

-- Create composite indexes for common queries
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action);
CREATE INDEX idx_audit_logs_entity_action ON audit_logs(entity_type, entity_id, action);
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action_created ON audit_logs(action, created_at DESC);

-- Create GIN indexes for JSONB columns
CREATE INDEX idx_audit_logs_old_values ON audit_logs USING GIN (old_values);
CREATE INDEX idx_audit_logs_new_values ON audit_logs USING GIN (new_values);
CREATE INDEX idx_audit_logs_metadata ON audit_logs USING GIN (metadata);

-- Create partial indexes for specific scenarios
CREATE INDEX idx_audit_logs_failed_logins ON audit_logs(user_id, created_at DESC) 
    WHERE action = 'LOGIN_FAILED';
CREATE INDEX idx_audit_logs_security_events ON audit_logs(created_at DESC) 
    WHERE action IN ('PASSWORD_CHANGE', 'PASSWORD_RESET', 'PERMISSION_CHANGE');

-- Add table and column comments
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for all system actions and security events';
COMMENT ON COLUMN audit_logs.user_id IS 'User who performed the action (NULL for system actions)';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity affected by the action';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID of the specific entity affected';
COMMENT ON COLUMN audit_logs.old_values IS 'Previous state before UPDATE or DELETE (JSON format)';
COMMENT ON COLUMN audit_logs.new_values IS 'New state after CREATE or UPDATE (JSON format)';
COMMENT ON COLUMN audit_logs.ip_address IS 'Client IP address (IPv4 or IPv6)';
COMMENT ON COLUMN audit_logs.user_agent IS 'Client user agent string (browser/app info)';
COMMENT ON COLUMN audit_logs.duration_ms IS 'Time taken to complete the action in milliseconds';
COMMENT ON COLUMN audit_logs.metadata IS 'Additional context in JSON format (e.g., request params, headers)';

-- Create function to automatically clean old audit logs (optional, for data retention)
CREATE OR REPLACE FUNCTION cleanup_old_audit_logs(retention_days INTEGER DEFAULT 365)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM audit_logs
    WHERE created_at < CURRENT_TIMESTAMP - (retention_days || ' days')::INTERVAL
    AND action NOT IN ('LOGIN_FAILED', 'PASSWORD_CHANGE', 'PERMISSION_CHANGE');  -- Keep security events longer
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_audit_logs IS 'Cleanup audit logs older than specified days (default 365), preserving critical security events';

-- Create view for recent security events
CREATE OR REPLACE VIEW recent_security_events AS
SELECT 
    al.id,
    al.user_id,
    u.username,
    u.email,
    al.action,
    al.entity_type,
    al.description,
    al.ip_address,
    al.created_at
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE al.action IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE', 'PASSWORD_RESET', 'PERMISSION_CHANGE')
AND al.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
ORDER BY al.created_at DESC;

COMMENT ON VIEW recent_security_events IS 'Recent security-related events from the last 30 days';

-- Create view for failed login attempts
CREATE OR REPLACE VIEW failed_login_attempts AS
SELECT 
    al.user_id,
    u.username,
    u.email,
    COUNT(*) as attempt_count,
    MAX(al.created_at) as last_attempt,
    array_agg(DISTINCT al.ip_address) as ip_addresses
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE al.action = 'LOGIN_FAILED'
AND al.created_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
GROUP BY al.user_id, u.username, u.email
HAVING COUNT(*) >= 3
ORDER BY attempt_count DESC, last_attempt DESC;

COMMENT ON VIEW failed_login_attempts IS 'Users with 3+ failed login attempts in the last 24 hours';

