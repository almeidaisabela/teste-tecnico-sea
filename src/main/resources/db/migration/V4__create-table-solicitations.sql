CREATE TABLE solicitations (
           id SERIAL PRIMARY KEY,
           client_id INTEGER NOT NULL REFERENCES users(id),

           status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
               CHECK (status IN ('DRAFT', 'SUBMITTED', 'IN_REVIEW', 'APPROVED', 'REJECTED')),

           current_step INTEGER NOT NULL DEFAULT 0
               CHECK (current_step BETWEEN 0 AND 3),

           service_type VARCHAR(20)
               CHECK (service_type IN ('INSTALLATION', 'MAINTENANCE', 'INSPECTION')),
           title VARCHAR(80),
           description VARCHAR(1000),

           cep VARCHAR(9),
           number VARCHAR(20),
           complement VARCHAR(100),
           street VARCHAR(150),
           neighborhood VARCHAR(100),
           city VARCHAR(100),
           state VARCHAR(2),

           priority VARCHAR(10)
               CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
           preferred_date DATE,
           estimated_value NUMERIC(12, 2),
           terms_accepted BOOLEAN,

           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
           updated_at TIMESTAMP,
           submitted_at TIMESTAMP,
           analyzed_at TIMESTAMP,
           analyzed_by INTEGER REFERENCES users(id),
           analysis_comment VARCHAR(1000)
);

CREATE INDEX idx_solicitations_client_id ON solicitations(client_id);
CREATE INDEX idx_solicitations_status ON solicitations(status);
CREATE INDEX idx_solicitations_state ON solicitations(state);