CREATE TABLE analyst_coverage_state (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    state VARCHAR(2) NOT NULL
        CHECK (state IN('AC', 'AL', 'AP', 'AM', 'BA', 'CE',
        'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA', 'PB',
        'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC',
        'SP', 'SE', 'TO')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, state)
);