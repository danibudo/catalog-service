CREATE TABLE titles (
    id               UUID         PRIMARY KEY,
    isbn             VARCHAR(13)  NOT NULL UNIQUE,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    genre            VARCHAR(100) NOT NULL,
    publication_year INTEGER      NOT NULL,
    description      TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);