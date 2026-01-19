CREATE SEQUENCE IF NOT EXISTS dataset_table_id_seq
    START 1
    INCREMENT 1
    MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

SELECT setval('dataset_table_id_seq', (SELECT COALESCE(MAX(id), 1) FROM dataset_table));

ALTER TABLE dataset_table
    ALTER COLUMN id SET DEFAULT nextval('dataset_table_id_seq');

GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO dataflow;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO dataset;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO recordstore;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO testuser;
GRANT USAGE, SELECT, UPDATE ON SEQUENCE public.dataset_table_id_seq TO validation;