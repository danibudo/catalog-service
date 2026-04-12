CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER titles_set_updated_at
    BEFORE UPDATE ON titles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER copies_set_updated_at
    BEFORE UPDATE ON copies
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();