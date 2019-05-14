CREATE SCHEMA dataset_1
    AUTHORIZATION root;
	
CREATE TABLE "dataset_1".record
(
    id integer NOT NULL,
    name text,
    CONSTRAINT record_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE "dataset_1".record
    OWNER to root;