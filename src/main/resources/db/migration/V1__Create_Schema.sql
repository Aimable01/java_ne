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
-- UTILITY BILLING SYSTEM TABLES
-- ============================================

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    national_id VARCHAR(16) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_national_id CHECK (national_id ~ '^1[0-9]{15}$')
);

-- Meters table
CREATE TABLE IF NOT EXISTS meters (
    id BIGSERIAL PRIMARY KEY,
    meter_number VARCHAR(50) UNIQUE NOT NULL,
    meter_type VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL,
    installation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_meter_type CHECK (meter_type IN ('WATER', 'ELECTRICITY')),
    CONSTRAINT chk_meter_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT fk_meter_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Meter readings table
CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGSERIAL PRIMARY KEY,
    meter_id BIGINT NOT NULL,
    previous_reading DOUBLE PRECISION NOT NULL,
    current_reading DOUBLE PRECISION NOT NULL,
    reading_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_reading CHECK (current_reading > previous_reading),
    CONSTRAINT fk_reading_meter FOREIGN KEY (meter_id) REFERENCES meters(id) ON DELETE CASCADE,
    CONSTRAINT uk_meter_reading UNIQUE (meter_id, reading_date)
);

-- Tariffs table
CREATE TABLE IF NOT EXISTS tariffs (
    id BIGSERIAL PRIMARY KEY,
    meter_type VARCHAR(20) NOT NULL,
    version INTEGER NOT NULL,
    effective_date DATE NOT NULL,
    rate_per_unit NUMERIC(10,2) NOT NULL,
    fixed_service_charge NUMERIC(10,2),
    vat_rate NUMERIC(5,2),
    late_payment_penalty_rate NUMERIC(5,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_tariff_meter_type CHECK (meter_type IN ('WATER', 'ELECTRICITY')),
    CONSTRAINT chk_tariff_rate CHECK (rate_per_unit > 0)
);

-- Bills table
CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    meter_id BIGINT NOT NULL,
    meter_reading_id BIGINT NOT NULL,
    tariff_id BIGINT NOT NULL,
    billing_month INTEGER NOT NULL,
    billing_year INTEGER NOT NULL,
    previous_reading DOUBLE PRECISION NOT NULL,
    current_reading DOUBLE PRECISION NOT NULL,
    consumption DOUBLE PRECISION NOT NULL,
    consumption_charge NUMERIC(12,2) NOT NULL,
    fixed_service_charge NUMERIC(12,2),
    vat_amount NUMERIC(12,2),
    penalty_amount NUMERIC(12,2),
    total_amount NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) DEFAULT 0,
    outstanding_balance NUMERIC(12,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_bill_status CHECK (status IN ('PENDING', 'APPROVED', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_billing_month CHECK (billing_month BETWEEN 1 AND 12),
    CONSTRAINT fk_bill_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_bill_meter FOREIGN KEY (meter_id) REFERENCES meters(id),
    CONSTRAINT fk_bill_reading FOREIGN KEY (meter_reading_id) REFERENCES meter_readings(id),
    CONSTRAINT fk_bill_tariff FOREIGN KEY (tariff_id) REFERENCES tariffs(id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    reference_number VARCHAR(100) UNIQUE NOT NULL,
    amount_paid NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    notes TEXT,
    payment_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'MOBILE_MONEY', 'CARD', 'CHEQUE')),
    CONSTRAINT chk_payment_amount CHECK (amount_paid > 0),
    CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id) REFERENCES bills(id)
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_bill_id BIGINT,
    related_payment_id BIGINT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    email_sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('BILL_GENERATED', 'PAYMENT_RECEIVED', 'PAYMENT_CONFIRMED', 'BILL_OVERDUE', 'ACCOUNT_UPDATE')),
    CONSTRAINT fk_notification_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
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

CREATE INDEX IF NOT EXISTS idx_customers_national_id ON customers(national_id);
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);

CREATE INDEX IF NOT EXISTS idx_meters_number ON meters(meter_number);
CREATE INDEX IF NOT EXISTS idx_meters_customer ON meters(customer_id);
CREATE INDEX IF NOT EXISTS idx_meters_type ON meters(meter_type);
CREATE INDEX IF NOT EXISTS idx_meters_status ON meters(status);

CREATE INDEX IF NOT EXISTS idx_meter_readings_meter ON meter_readings(meter_id);
CREATE INDEX IF NOT EXISTS idx_meter_readings_date ON meter_readings(reading_date);

CREATE INDEX IF NOT EXISTS idx_tariffs_type ON tariffs(meter_type);
CREATE INDEX IF NOT EXISTS idx_tariffs_effective ON tariffs(effective_date);
CREATE INDEX IF NOT EXISTS idx_tariffs_active ON tariffs(active);

CREATE INDEX IF NOT EXISTS idx_bills_customer ON bills(customer_id);
CREATE INDEX IF NOT EXISTS idx_bills_meter ON bills(meter_id);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills(status);
CREATE INDEX IF NOT EXISTS idx_bills_billing_period ON bills(billing_year, billing_month);
CREATE INDEX IF NOT EXISTS idx_bills_due_date ON bills(due_date);

CREATE INDEX IF NOT EXISTS idx_payments_bill ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_reference ON payments(reference_number);

CREATE INDEX IF NOT EXISTS idx_notifications_customer ON notifications(customer_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(read);

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
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO active_users FROM users WHERE status = 'ACTIVE';
    SELECT COUNT(*) INTO disabled_users FROM users WHERE status = 'DISABLED';
    SELECT jsonb_object_agg(role, count) INTO users_by_role
    FROM (SELECT role, COUNT(*) as count FROM user_roles GROUP BY role) AS role_counts;
END;
$$;

-- Procedure to generate bill notification
CREATE OR REPLACE PROCEDURE generate_bill_notification(
    p_customer_id BIGINT,
    p_bill_id BIGINT,
    p_month INTEGER,
    p_year INTEGER,
    p_amount NUMERIC
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_name VARCHAR(255);
    v_customer_email VARCHAR(255);
    v_month_name VARCHAR(20);
    v_subject VARCHAR(255);
    v_message TEXT;
BEGIN
    SELECT full_name, email INTO v_customer_name, v_customer_email
    FROM customers WHERE id = p_customer_id;
    
    SELECT to_char(to_date(p_month::text, 'MM'), 'Month') INTO v_month_name;
    
    v_subject := 'Your Utility Bill for ' || v_month_name || ' ' || p_year;
    v_message := 'Dear ' || v_customer_name || ',\n\nYour ' || v_month_name || ' ' || p_year || 
                 ' utility bill of ' || p_amount::text || ' FRW has been generated successfully.\n\n' ||
                 'Please ensure payment is made before the due date to avoid late payment penalties.\n\n' ||
                 'Thank you for your business.';
    
    INSERT INTO notifications (customer_id, notification_type, subject, message, related_bill_id)
    VALUES (p_customer_id, 'BILL_GENERATED', v_subject, v_message, p_bill_id);
    
    RAISE NOTICE 'Bill notification generated for customer %', v_customer_name;
END;
$$;

-- Procedure to create payment notification
CREATE OR REPLACE PROCEDURE create_payment_notification(
    p_customer_id BIGINT,
    p_bill_id BIGINT,
    p_payment_id BIGINT,
    p_amount NUMERIC,
    p_payment_date DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_name VARCHAR(255);
    v_customer_email VARCHAR(255);
    v_subject VARCHAR(255);
    v_message TEXT;
BEGIN
    SELECT full_name, email INTO v_customer_name, v_customer_email
    FROM customers WHERE id = p_customer_id;
    
    v_subject := 'Payment Received';
    v_message := 'Dear ' || v_customer_name || ',\n\nYour payment of ' || p_amount::text || 
                 ' FRW has been received successfully on ' || p_payment_date::text || '.\n\n' +
                 'Thank you for your payment.';
    
    INSERT INTO notifications (customer_id, notification_type, subject, message, related_bill_id, related_payment_id)
    VALUES (p_customer_id, 'PAYMENT_RECEIVED', v_subject, v_message, p_bill_id, p_payment_id);
    
    RAISE NOTICE 'Payment notification generated for customer %', v_customer_name;
END;
$$;

-- Procedure to update bill status on full payment
CREATE OR REPLACE PROCEDURE update_bill_status_on_full_payment(
    p_bill_id BIGINT,
    p_customer_id BIGINT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_name VARCHAR(255);
    v_month INTEGER;
    v_year INTEGER;
    v_amount NUMERIC;
    v_month_name VARCHAR(20);
    v_subject VARCHAR(255);
    v_message TEXT;
BEGIN
    SELECT full_name INTO v_customer_name FROM customers WHERE id = p_customer_id;
    SELECT billing_month, billing_year, total_amount INTO v_month, v_year, v_amount
    FROM bills WHERE id = p_bill_id;
    
    SELECT to_char(to_date(v_month::text, 'MM'), 'Month') INTO v_month_name;
    
    UPDATE bills SET status = 'PAID' WHERE id = p_bill_id;
    
    v_subject := 'Bill Fully Paid - ' || v_month_name || ' ' || v_year;
    v_message := 'Dear ' || v_customer_name || ',\n\nYour ' || v_month_name || ' ' || v_year || 
                 ' utility bill of ' || v_amount::text || ' FRW has been successfully processed and fully paid.\n\n' +
                 'Thank you for your prompt payment. We appreciate your business.';
    
    INSERT INTO notifications (customer_id, notification_type, subject, message, related_bill_id)
    VALUES (p_customer_id, 'PAYMENT_CONFIRMED', v_subject, v_message, p_bill_id);
    
    RAISE NOTICE 'Bill status updated to PAID and notification sent for bill %', p_bill_id;
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
        VALUES ('users', 'INSERT', NEW.id, to_jsonb(NEW), COALESCE(NEW.email, 'SYSTEM'));
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, new_data, changed_by)
        VALUES ('users', 'UPDATE', NEW.id, to_jsonb(OLD), to_jsonb(NEW), COALESCE(NEW.email, 'SYSTEM'));
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, changed_by)
        VALUES ('users', 'DELETE', OLD.id, to_jsonb(OLD), COALESCE(OLD.email, 'SYSTEM'));
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_audit_users ON users;
CREATE TRIGGER trigger_audit_users
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_user_changes();

-- Function to audit bill changes
CREATE OR REPLACE FUNCTION audit_bill_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (table_name, operation, record_id, new_data, changed_by)
        VALUES ('bills', 'INSERT', NEW.id, to_jsonb(NEW), 'SYSTEM');
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, new_data, changed_by)
        VALUES ('bills', 'UPDATE', NEW.id, to_jsonb(OLD), to_jsonb(NEW), 'SYSTEM');
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (table_name, operation, record_id, old_data, changed_by)
        VALUES ('bills', 'DELETE', OLD.id, to_jsonb(OLD), 'SYSTEM');
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_audit_bills ON bills;
CREATE TRIGGER trigger_audit_bills
    AFTER INSERT OR UPDATE OR DELETE ON bills
    FOR EACH ROW EXECUTE FUNCTION audit_bill_changes();

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_customers_timestamp ON customers;
CREATE TRIGGER trigger_update_customers_timestamp
    BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_meters_timestamp ON meters;
CREATE TRIGGER trigger_update_meters_timestamp
    BEFORE UPDATE ON meters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_meter_readings_timestamp ON meter_readings;
CREATE TRIGGER trigger_update_meter_readings_timestamp
    BEFORE UPDATE ON meter_readings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_tariffs_timestamp ON tariffs;
CREATE TRIGGER trigger_update_tariffs_timestamp
    BEFORE UPDATE ON tariffs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_bills_timestamp ON bills;
CREATE TRIGGER trigger_update_bills_timestamp
    BEFORE UPDATE ON bills
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trigger_update_payments_timestamp ON payments;
CREATE TRIGGER trigger_update_payments_timestamp
    BEFORE UPDATE ON payments
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
    OPEN user_cursor;
    LOOP
        FETCH user_cursor INTO v_user_id, v_user_email;
        EXIT WHEN NOT FOUND;
        
        UPDATE users SET status = p_status WHERE id = v_user_id;
        
        v_updated_count := v_updated_count + 1;
        v_batch_count := v_batch_count + 1;
        
        IF v_batch_count >= p_batch_size THEN
            COMMIT;
            v_batch_count := 0;
            RAISE NOTICE 'Batch committed. Updated % users so far', v_updated_count;
        END IF;
    END LOOP;
    
    CLOSE user_cursor;
    
    IF v_batch_count > 0 THEN
        COMMIT;
    END IF;
    
    INSERT INTO audit_log (table_name, operation, new_data, changed_by)
    VALUES ('users', 'BATCH_UPDATE', jsonb_build_object('status', p_status, 'updated_count', v_updated_count), 'SYSTEM');
    
    RAISE NOTICE 'Batch update completed. Total users updated: %', v_updated_count;
END;
$$;

-- ============================================
-- CREATE VIEWS
-- ============================================

CREATE OR REPLACE VIEW user_summary AS
SELECT 
    u.id, u.code, u.first_name, u.last_name, u.email, u.mobile, u.status, u.created_at,
    ARRAY_AGG(ur.role) FILTER (WHERE ur.role IS NOT NULL) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.code, u.first_name, u.last_name, u.email, u.mobile, u.status, u.created_at;

CREATE OR REPLACE VIEW audit_summary AS
SELECT 
    table_name, operation, COUNT(*) as operation_count,
    MIN(changed_at) as first_occurrence, MAX(changed_at) as last_occurrence
FROM audit_log
GROUP BY table_name, operation
ORDER BY table_name, operation;

CREATE OR REPLACE VIEW customer_summary AS
SELECT 
    c.id, c.full_name, c.national_id, c.email, c.phone_number, c.status,
    COUNT(m.id) as meter_count,
    COALESCE(SUM(b.total_amount), 0) as total_billed,
    COALESCE(SUM(b.amount_paid), 0) as total_paid
FROM customers c
LEFT JOIN meters m ON c.id = m.customer_id
LEFT JOIN bills b ON c.id = b.customer_id
GROUP BY c.id, c.full_name, c.national_id, c.email, c.phone_number, c.status;

-- ============================================
-- MIGRATION COMPLETE
-- ============================================
