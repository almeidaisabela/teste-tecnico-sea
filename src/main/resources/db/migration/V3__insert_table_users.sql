INSERT INTO users (name, email, password_hash, role, enabled, created_at)
VALUES (
           'Admin',
           'admin@sistema.com',
           '$2a$10$x5XTpefSz2RkgMAEVlNAIuczVBo7IQ6B48SRe3T6ET7dx1PG.74ei',
           'ADMIN',
           true,
           now()
       )
    ON CONFLICT (email) DO NOTHING;