-- Enable extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. USERS TABLE
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(256) NOT NULL,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. APPLICATIONS TABLE
CREATE TABLE applications (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              name VARCHAR(100) UNIQUE NOT NULL,
                              description TEXT
);

-- 3. PERMISSIONS TABLE
CREATE TABLE permissions (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name VARCHAR(100) UNIQUE NOT NULL,
                             description TEXT
);

-- 4. ROLES TABLE
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(100) NOT NULL,
                       description TEXT,
                       is_custom BOOLEAN DEFAULT FALSE
);

-- 5. USER_ROLES TABLE (User assigned roles per application)
CREATE TABLE user_roles (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
                            UNIQUE (user_id, role_id, application_id)
);

-- 6. APPLICATION_PERMISSIONS TABLE (Permissions linked to applications)
CREATE TABLE application_permissions (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
                                         permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                         UNIQUE (application_id, permission_id)
);

-- 7. ROLE_PERMISSIONS TABLE (Roles assigned permissions)
CREATE TABLE role_permissions (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                                  permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  UNIQUE (role_id, permission_id)
);

-- 8. AUDIT_EVENT_TYPE TABLE
CREATE TABLE audit_event_type (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  description VARCHAR(50) NOT NULL
);

-- 9. AUDIT_LOGS TABLE
CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            event_type REFERENCES audit_event_type(id) ON DELETE SET NULL,
                            user_id UUID REFERENCES users(id) ON DELETE SET NULL,
                            application_name VARCHAR(100),
                            description TEXT,
                            metadata JSONB
);

-- 10. REVOKED TOKENS TABLE
CREATE TABLE revoked_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                token_hash VARCHAR(64) NOT NULL UNIQUE,
                                user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
