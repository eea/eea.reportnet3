CREATE TABLE release_receipt (
    id BIGSERIAL PRIMARY KEY,
    dataflow_id BIGINT NOT NULL REFERENCES public.dataflow(id) ON DELETE CASCADE,
    user_custom_text TEXT DEFAULT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);