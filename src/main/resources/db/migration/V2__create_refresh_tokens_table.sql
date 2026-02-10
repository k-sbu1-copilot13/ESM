-- Migration: Create refresh_tokens table
-- Description: Add support for JWT refresh token mechanism
-- This enables secure token rotation and logout functionality

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint with cascade delete
    -- When user is deleted, all their refresh tokens are also deleted
    CONSTRAINT fk_refresh_token_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index for fast token lookup during refresh requests
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Index for querying all tokens for a specific user
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- Index for efficient cleanup of expired tokens
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);

-- Index for filtering active (non-revoked) tokens
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- Comments for documentation
COMMENT ON TABLE refresh_tokens IS 'Stores JWT refresh tokens for user session management';
COMMENT ON COLUMN refresh_tokens.token IS 'UUID string representing the refresh token';
COMMENT ON COLUMN refresh_tokens.expiry_date IS 'Token expiration timestamp';
COMMENT ON COLUMN refresh_tokens.revoked IS 'Flag indicating if token has been manually revoked';
COMMENT ON COLUMN refresh_tokens.created_at IS 'Timestamp when token was created';
