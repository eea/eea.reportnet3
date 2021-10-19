CREATE TABLE IF NOT EXISTS public.webforms (
	id bigserial NOT NULL,
	name varchar NULL,
	CONSTRAINT webforms_pk PRIMARY KEY (id)
);

GRANT ALL ON TABLE public.webforms TO testuser, dataflow, dataset, validation, recordstore;