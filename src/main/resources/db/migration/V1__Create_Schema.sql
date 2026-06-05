-- ============================================
-- INITIAL DATABASE SCHEMA MIGRATION
-- ============================================
-- This migration creates all tables, procedures, triggers, and demonstrates cursor usage
-- ============================================

-- ============================================
-- CREATE TABLES
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    mobile VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

-- User roles junction table (for many-to-many relationship)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Examples table
CREATE TABLE IF NOT EXISTS examples (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OTP table
CREATE TABLE IF NOT EXISTS otps (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    CONSTRAINT chk_expires CHECK (expires_at > created_at)
);

-- ============================================
-- CREATE INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_code ON users(code);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

CREATE INDEX IF NOT EXISTS idx_examples_name ON examples(name);
CREATE INDEX IF NOT EXISTS idx_examples_created_at ON examples(created_at);

CREATE INDEX IF NOT EXISTS idx_otps_email ON otps(email);
CREATE INDEX IF NOT EXISTS idx_otps_code ON otps(code);
CREATE INDEX IF NOT EXISTS idx_otps_expires_at ON otps(expires_at);

-- ============================================
-- CREATE AUDIT TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(20) NOT NULL,
    record_id BIGINT,
    old_data JSONB,
    new_data JSONB,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_operation ON audit_log(operation);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON audit_log(changed_at);

-- ============================================
-- CREATE DATABASE PROCEDURES
-- ============================================

-- Procedure to get user statistics
CREATE OR REPLACE PROCEDURE get_user_statistics(
    OUT total_users BIGINT,
    OUT active_users BIGINT,
    OUT disabled_users BIGINT,
    OUT users_by_role JSONB
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Get total users
    SELECT COUNT(*) INTO total_users FROM users;
    
    -- Get active users
    SELECT COUNT(*) INTO active_users FROM users WHERE status = 'ACTIVE';
    
    -- Get disabled users
    SELECT COUNT(*) INTO disabled_users FROM users WHERE status = 'DISABLED';
    
    -- Get users by role
    SELECT jsonb_object_agg(role, count) INTO users_by_role
    FROM (
        SELECT role, COUNT(*) as count
        FROM user_roles
        GROUP BY role
    ) AS role_counts;
END;
$$;

-- Procedure to soft delete user (change status to DISABLED)
CREATE OR REPLACE PROCEDURE soft_delete_user(p_user_id BIGINT, p_changed_by VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    v_email VARCHAR(255);
BEGIN
    -- Get user email before deletion
    SELECT email INTO v_email FROM users WHERE id = p_user_id;
    
    IF v_email IS NULL THEN
        RAISE EXCEPTION 'User with ID % not found', p_user_id;
    END IF;
    
    -- Update user status to DISABLED
    UPDATE users 
    SET status = 'DISABLED' 
    WHERE id = p_user_id;
    
    -- Log the operation
    INSERT INTO audit_log (table_name, operation, record_id, new_data, changed_by)
    VALUES ('users', 'SOFT_DELETE', p_user_id, 
            jsonb_build_object('email', v_email, 'status', 'DISABLED'), 
            p_changed_by);
    
    RAISE NOTICE 'User % has been soft deleted', v_email;
END;
$$;

-- Procedure to promote user to admin
CREATE OR REPLACE PROCEDURE promote_user_to_admin(p_user_id BIGINT, p_changed_by VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    v_email VARCHAR(255);
BEGIN
    -- Get user email
    SELECT email INTO v_email FROM users WHERE id = p_user_id;
    
    IF v_email IS NULL THEN
        RAISE EXCEPTION 'User with ID % not found', p_user_id;
    END IF;
    
    -- Remove existing roles
    DELETE FROM user_roles WHERE user_id = p_user_id;
    
    -- Add admin role
    INSERT INTO user_roles (user_id, role) VALUES (p_user_id, 'ROLE_ADMIN');
    
    -- Log the operation
    INSERT INTO audit_log (table_name, operation, record_id, new_data, changed_by)
    VALUES ('user_roles', 'PROMOTE_TO_ADMIN', p_user_id, 
            jsonb_build_object('email', v_email, 'role', 'ROLE_ADMIN'), 
            p_changed_by);
    
    RAISE NOTICE 'User % has been promoted to admin', v_email;
END;
$$;

-- Procedure to clean expired OTPs
CREATE OR REPLACE PROCEDURE clean_expired_otps()
LANGUAGE plpgsql
AS $$
DECLARE
    v_deleted_count INTEGER;
BEGIN
    -- Delete expired OTPs
    DELETE FROM otps 
    WHERE expires_at < CURRENT_TIMESTAMP OR used = TRUE;
    
    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;
    
    RAISE NOTICE 'Deleted % expired/used OTPs', v_deleted_count;
    
    -- Log the cleanup
    INSERT INTO audit_log (table_name, operation, new_data, changed_by)
    VALUES ('otps', 'CLEANUP', 
            jsonb_build_object('deleted_count', v_deleted_count), 
            'SYSTEM');
END;
$$;

-- ============================================
-- CREATE DATABASE TRIGGERS
-- ============================================

-- Function to audit user changes
CREATE OR REPLACE FUNCTION audit_user_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (table_name, operation, record_id, new_data, changed_by)
        VALUES ('users', 'INSERT', NEW.id, 
                to_jsonb(NEW), 
                COALESCE(NEW.email, 'SYSTEM'));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, new_data, changed_by)
        VALUES ('users', 'UPDATE', NEW.id, 
                to_jsonb(OLD), 
                to_jsonb(NEW), 
                COALESCE(NEW.email, 'SYSTEM'));
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, changed_by)
        VALUES ('users', 'DELETE', OLD.id, 
                to_jsonb(OLD), 
                COALESCE(OLD.email, 'SYSTEM'));
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger for user table
DROP TRIGGER IF EXISTS trigger_audit_users ON users;
CREATE TRIGGER trigger_audit_users
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_user_changes();

-- Function to audit example changes
CREATE OR REPLACE FUNCTION audit_example_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (table_name, operation, record_id, new_data, changed_by)
        VALUES ('examples', 'INSERT', NEW.id, 
                to_jsonb(NEW), 
                'SYSTEM');
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, new_data, changed_by)
        VALUES ('examples', 'UPDATE', NEW.id, 
                to_jsonb(OLD), 
                to_jsonb(NEW), 
                'SYSTEM');
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, changed_by)
        VALUES ('examples', 'DELETE', OLD.id, 
                to_jsonb(OLD), 
                'SYSTEM');
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger for examples table
DROP TRIGGER IF EXISTS trigger_audit_examples ON examples;
CREATE TRIGGER trigger_audit_examples
    AFTER INSERT OR UPDATE OR DELETE ON examples
    FOR EACH ROW EXECUTE FUNCTION audit_example_changes();

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for examples updated_at
DROP TRIGGER IF EXISTS trigger_update_examples_timestamp ON examples;
CREATE TRIGGER trigger_update_examples_timestamp
    BEFORE UPDATE ON examples
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- CREATE DATABASE CURSOR EXAMPLE (Stored Procedure)
-- ============================================

-- Procedure that demonstrates cursor usage for batch processing
CREATE OR REPLACE PROCEDURE batch_update_user_status(p_status VARCHAR, p_batch_size INTEGER DEFAULT 100)
LANGUAGE plpgsql
AS $$
DECLARE
    user_cursor CURSOR FOR SELECT id, email FROM users WHERE status != p_status;
    v_user_id BIGINT;
    v_user_email VARCHAR(255);
    v_updated_count INTEGER := 0;
    v_batch_count INTEGER := 0;
BEGIN
    -- Open cursor
    OPEN user_cursor;
    
    -- Loop through cursor
    LOOP
        -- Fetch next batch of records
        FETCH user_cursor INTO v_user_id, v_user_email;
        EXIT WHEN NOT FOUND;
        
        -- Update user status
        UPDATE users 
        SET status = p_status 
        WHERE id = v_user_id;
        
        v_updated_count := v_updated_count + 1;
        v_batch_count := v_batch_count + 1;
        
        -- Commit batch
        IF v_batch_count >= p_batch_size THEN
            COMMIT;
            v_batch_count := 0;
            RAISE NOTICE 'Batch committed. Updated % users so far', v_updated_count;
        END IF;
    END LOOP;
    
    -- Close cursor
    CLOSE user_cursor;
    
    -- Final commit for remaining records
    IF v_batch_count > 0 THEN
        COMMIT;
    END IF;
    
    -- Log the batch operation
    INSERT INTO audit_log (table_name, operation, new_data, changed_by)
    VALUES ('users', 'BATCH_UPDATE', 
            jsonb_build_object('status', p_status, 'updated_count', v_updated_count), 
            'SYSTEM');
    
    RAISE NOTICE 'Batch update completed. Total users updated: %', v_updated_count;
END;
$$;

-- ============================================
-- CREATE VIEWS
-- ============================================

-- View for user summary with roles
CREATE OR REPLACE VIEW user_summary AS
SELECT 
    u.id,
    u.code,
    u.first_name,
    u.last_name,
    u.email,
    u.mobile,
    u.status,
    u.created_at,
    ARRAY_AGG(ur.role) FILTER (WHERE ur.role IS NOT NULL) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.code, u.first_name, u.last_name, u.email, u.mobile, u.status, u.created_at;

-- View for audit log summary
CREATE OR REPLACE VIEW audit_summary AS
SELECT 
    table_name,
    operation,
    COUNT(*) as operation_count,
    MIN(changed_at) as first_occurrence,
    MAX(changed_at) as last_occurrence
FROM audit_log
GROUP BY table_name, operation
ORDER BY table_name, operation;

-- ============================================
-- INSERT INITIAL DATA (Optional)
-- ============================================

-- Note: Initial data seeding is handled by DataSeeder.java
-- This migration only creates the schema structure

-- ============================================
-- MIGRATION COMPLETE
-- ============================================
