CREATE TABLE copies (
    id         UUID        PRIMARY KEY,
    title_id   UUID        NOT NULL REFERENCES titles(id),
    condition  VARCHAR(20) NOT NULL CHECK (condition IN ('good', 'damaged')),
    status     VARCHAR(20) NOT NULL DEFAULT 'available' CHECK (status IN ('available', 'on_loan', 'decommissioned')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);