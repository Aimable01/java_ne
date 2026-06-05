# Database Migrations Guide

This document provides comprehensive information about database migrations in this project using Flyway.

## Overview

This project uses **Flyway** for database version control and migrations. All database schema changes are managed through SQL migration files located in `src/main/resources/db/migration/`.

## Migration Files

### V1__Create_Schema.sql

The initial migration (`V1__Create_Schema.sql`) creates the complete database schema including:

#### Tables
- **users** - User accounts with authentication and profile information
- **user_roles** - Junction table for user-role many-to-many relationships
- **examples** - Example entity for demonstration purposes
- **otps** - One-time password codes for authentication
- **audit_log** - Audit trail for all database operations

#### Indexes
- Performance indexes on frequently queried columns
- Composite indexes for common query patterns

#### Database Procedures
1. **get_user_statistics** - Returns user statistics (total, active, disabled, by role)
2. **soft_delete_user** - Soft deletes a user by setting status to DISABLED
3. **promote_user_to_admin** - Promotes a user to admin role
4. **clean_expired_otps** - Removes expired and used OTP codes
5. **batch_update_user_status** - Demonstrates cursor usage for batch operations

#### Database Triggers
1. **trigger_audit_users** - Automatically logs all user table changes
2. **trigger_audit_examples** - Automatically logs all example table changes
3. **trigger_update_examples_timestamp** - Auto-updates updated_at timestamp

#### Views
1. **user_summary** - User information with aggregated roles
2. **audit_summary** - Summary of audit log operations

## Applying Migrations

### Automatic Migration (Recommended)

Migrations run automatically when the application starts if Flyway is enabled:

```properties
spring.flyway.enabled=true
```

The application will:
1. Check the `flyway_schema_history` table
2. Compare with migration files in `db/migration`
3. Apply any pending migrations in version order

### Manual Migration via Maven

To apply migrations manually without starting the application:

```bash
# Windows
.\mvnw.cmd flyway:migrate

# Linux/Mac
./mvnw flyway:migrate
```

### Manual Migration via Flyway CLI

If you have Flyway installed globally:

```bash
flyway -url=jdbc:postgresql://localhost:5432/mydb_java \
       -user=postgres \
       -password=postgres \
       -locations=classpath:db/migration \
       migrate
```

## Migration Status Check

### Check Migration Status via Maven

```bash
# Windows
.\mvnw.cmd flyway:info

# Linux/Mac
./mvnw flyway:info
```

### Check Migration Status via Flyway CLI

```bash
flyway -url=jdbc:postgresql://localhost:5432/mydb_java \
       -user=postgres \
       -password=postgres \
       -locations=classpath:db/migration \
       info
```

## Creating New Migrations

### Naming Convention

Migration files must follow the naming pattern: `V{version}__{description}.sql`

- **Version**: Sequential number (e.g., V1, V2, V3)
- **Description**: Short description with underscores (e.g., Create_Schema, Add_Email_Index)

### Example: Creating a New Migration

1. Create a new file in `src/main/resources/db/migration/`
2. Name it: `V2__Add_User_Profile_Table.sql`
3. Write your SQL:

```sql
-- Add user profile table
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

4. The migration will be applied automatically on next application restart

## Rolling Back Migrations

Flyway Community Edition does not support automatic rollbacks. To rollback:

### Option 1: Create a New Migration

Create a new migration that reverses the changes:

```sql
-- V3__Rollback_User_Profile_Table.sql
DROP TABLE IF EXISTS user_profiles;
```

### Option 2: Manual Rollback

Manually execute the reverse SQL commands in your database tool.

## Database Procedures Usage

### Calling Procedures from Java

```java
@Repository
public class UserRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void callSoftDeleteUser(Long userId, String changedBy) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("soft_delete_user");
        
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", userId);
        inParams.put("p_changed_by", changedBy);
        
        jdbcCall.execute(inParams);
    }
    
    public Map<String, Object> getUserStatistics() {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("get_user_statistics")
            .declareParameters(
                new SqlParameter("total_users", Types.BIGINT),
                new SqlParameter("active_users", Types.BIGINT),
                new SqlParameter("disabled_users", Types.BIGINT),
                new SqlParameter("users_by_role", Types.OTHER)
            );
        
        return jdbcCall.execute();
    }
}
```

### Calling Procedures via SQL

```sql
-- Get user statistics
CALL get_user_statistics();

-- Soft delete user
CALL soft_delete_user(1, 'admin@app.com');

-- Promote user to admin
CALL promote_user_to_admin(2, 'admin@app.com');

-- Clean expired OTPs
CALL clean_expired_otps();

-- Batch update user status
CALL batch_update_user_status('ACTIVE', 100);
```

## Database Triggers

Triggers are automatically executed on specified events:

### User Audit Trigger
- **Event**: INSERT, UPDATE, DELETE on users table
- **Action**: Logs changes to audit_log table
- **Data captured**: Old and new values, operation type, timestamp

### Example Audit Trigger
- **Event**: INSERT, UPDATE, DELETE on examples table
- **Action**: Logs changes to audit_log table

### Auto-Update Timestamp Trigger
- **Event**: UPDATE on examples table
- **Action**: Automatically updates updated_at column

## Database Cursor Example

The `batch_update_user_status` procedure demonstrates cursor usage:

```sql
CALL batch_update_user_status('ACTIVE', 100);
```

This procedure:
1. Declares a cursor to fetch users
2. Processes records in batches (default 100)
3. Commits after each batch
4. Logs the operation in audit_log

## Audit Log

All important database operations are logged in the `audit_log` table:

```sql
-- View recent audit entries
SELECT * FROM audit_log ORDER BY changed_at DESC LIMIT 10;

-- View audit summary
SELECT * FROM audit_summary;

-- View audit for specific table
SELECT * FROM audit_log WHERE table_name = 'users' ORDER BY changed_at DESC;
```

## Troubleshooting

### Migration Fails on Startup

1. Check the error message in logs
2. Verify database connection settings
3. Ensure user has necessary permissions
4. Check for syntax errors in migration SQL
5. Verify migration file naming convention

### Migration Already Applied Error

If Flyway reports a migration as already applied:

```bash
# Repair Flyway metadata (use with caution)
.\mvnw.cmd flyway:repair
```

### Schema Mismatch

If Hibernate entities don't match database schema:

1. Check `spring.jpa.hibernate.ddl-auto=validate` in application.properties
2. Ensure all migrations are applied
3. Verify entity annotations match table structure

## Best Practices

1. **Always review migrations** before committing to version control
2. **Test migrations** on a development database first
3. **Use transactions** in migration SQL for atomicity
4. **Add comments** to explain complex migrations
5. **Keep migrations small** and focused on single changes
6. **Never modify** existing migration files - create new ones
7. **Backup database** before applying major migrations
8. **Document breaking changes** in migration comments

## Migration Workflow

1. **Development**: Create migration file in `db/migration`
2. **Testing**: Run `mvn flyway:migrate` to test locally
3. **Review**: Have team review the migration SQL
4. **Commit**: Commit migration file with code changes
5. **Deploy**: Migration runs automatically on deployment

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool-flyway)
- [PostgreSQL Procedures](https://www.postgresql.org/docs/current/xproc.html)
- [PostgreSQL Triggers](https://www.postgresql.org/docs/current/sql-createtrigger.html)
