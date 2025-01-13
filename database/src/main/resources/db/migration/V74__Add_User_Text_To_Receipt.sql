CREATE TABLE release_receipt (
                                 id BIGSERIAL PRIMARY KEY,
                                 dataflow_id BIGINT NOT NULL UNIQUE,
                                 note TEXT DEFAULT NULL,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (dataflow_id) REFERENCES public.dataflow(id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS public.release_receipt_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

--Create Indexes--
CREATE INDEX IF NOT EXISTS release_receipt_dataflow_id ON dataflow (id);

--Grant permissions--
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.release_receipt TO dataflow;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.release_receipt TO dataset;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.release_receipt TO recordstore;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.release_receipt TO testuser;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.release_receipt TO validation;

GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.release_receipt_id_seq TO dataflow;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.release_receipt_id_seq TO dataset;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.release_receipt_id_seq TO recordstore;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.release_receipt_id_seq TO testuser;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.release_receipt_id_seq TO validation;
