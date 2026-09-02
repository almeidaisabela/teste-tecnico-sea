CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            user_id INTEGER,
                            role VARCHAR(20),
                            action VARCHAR(60) NOT NULL,
                            entity_id INTEGER,
                            duration_ms BIGINT NOT NULL,
                            success BOOLEAN NOT NULL,
                            error_message VARCHAR(500),
                            created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);