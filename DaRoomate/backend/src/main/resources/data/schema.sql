BEGIN;


CREATE TABLE IF NOT EXISTS users
(
    id SERIAL PRIMARY KEY,
    user_id CHARACTER VARYING(255) NOT NULL,
    first_name CHARACTER VARYING(50),
    last_name CHARACTER VARYING(50),
    email CHARACTER VARYING(100),
    phone CHARACTER VARYING(30),
    bio CHARACTER VARYING(255),
    reference_id CHARACTER VARYING(255),
    image_url CHARACTER VARYING(255),
    qr_code_secret CHARACTER VARYING(255),
    qr_code_img_uri TEXT,
    login_attempts INTEGER DEFAULT 0,
    last_login TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_attempt TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    mfa BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_expired BOOLEAN NOT NULL DEFAULT FALSE,
    account_non_locked BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_user_id UNIQUE (user_id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE

    );

CREATE TABLE IF NOT EXISTS chat_room_entity
(
    chat_id character varying(255) COLLATE pg_catalog."default",
    id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    recipient_id character varying(255) COLLATE pg_catalog."default",
    sender_id character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT chat_room_entity_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS confirmations
(
    id SERIAL PRIMARY KEY,
    code CHARACTER VARYING(255) NOT NULL,
    user_id BIGINT NOT NULL,
    reference_id CHARACTER VARYING(255) NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_confirmations_user_id UNIQUE (user_id),
    CONSTRAINT uq_confirmations_code UNIQUE (code),
    CONSTRAINT fk_confirmations_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_confirmations_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_confirmations_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS credentials
(
    id SERIAL PRIMARY KEY,
    password CHARACTER VARYING(255) NOT NULL,
    user_id BIGINT NOT NULL,
    reference_id CHARACTER VARYING(255) NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_credentials_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_credentials_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_credentials_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS documents
(
    id SERIAL PRIMARY KEY,
    document_id CHARACTER VARYING(255) NOT NULL,
    extension CHARACTER VARYING(10),
    formatted_size CHARACTER VARYING(255),
    icon CHARACTER VARYING(255),
    name CHARACTER VARYING(255),
    size BIGINT NOT NULL,
    uri CHARACTER VARYING(255),
    description CHARACTER VARYING(255),
    reference_id CHARACTER VARYING(255),
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_documents_document_id UNIQUE (document_id),
    CONSTRAINT fk_documents_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_documents_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS roles
(
    id SERIAL PRIMARY KEY,
    reference_id CHARACTER VARYING(255),
    permissions CHARACTER VARYING(255),
    role_name CHARACTER VARYING(255),
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_roles
(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES users (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS event_entity(
      id UUID PRIMARY KEY,
      title VARCHAR(255),
      description TEXT,
      start_time TIMESTAMP,
      end_time TIMESTAMP,
      room_id UUID,
      user_id UUID,
      created TIMESTAMP,
      updated TIMESTAMP,
      CONSTRAINT fk_room FOREIGN KEY (room_id) REFERENCES room (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
      CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS index_users_emails ON users (email);
CREATE INDEX IF NOT EXISTS index_users_user_id ON users (user_id);
CREATE INDEX IF NOT EXISTS index_confirmations_user_id ON confirmations (user_id);
CREATE INDEX IF NOT EXISTS index_credentials_user_id ON credentials (user_id);
CREATE INDEX IF NOT EXISTS index_user_roles_user_id ON user_roles (user_id);

    END;