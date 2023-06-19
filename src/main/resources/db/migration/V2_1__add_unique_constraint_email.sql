ALTER TABLE users
ADD CONSTRAINT uc_email UNIQUE (email);
