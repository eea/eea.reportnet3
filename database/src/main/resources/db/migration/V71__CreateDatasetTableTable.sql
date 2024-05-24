--Create table and sequence for dataset_table--
CREATE TABLE IF NOT EXISTS public.dataset_table (
	id int8 NOT NULL,
	dataset_id bigserial NOT NULL,
	dataset_schema_id varchar(255) NOT NULL,
	table_schema_id varchar(255) NOT NULL,
	is_iceberg_table_created bool NOT NULL DEFAULT false,
	CONSTRAINT dataset_table_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.dataset_table_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

--Create Indexes--
CREATE INDEX IF NOT EXISTS dataset_table_dataset_id ON dataset_table (dataset_id);
CREATE INDEX IF NOT EXISTS dataset_table_schema_id ON dataset_table (table_schema_id);

--Grant permissions--
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.dataset_table TO dataflow;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.dataset_table TO dataset;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.dataset_table TO recordstore;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.dataset_table TO testuser;
GRANT DELETE, REFERENCES, INSERT, TRUNCATE, TRIGGER, SELECT, UPDATE ON TABLE public.dataset_table TO validation;

GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO dataflow;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO dataset;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO recordstore;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO testuser;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO validation;