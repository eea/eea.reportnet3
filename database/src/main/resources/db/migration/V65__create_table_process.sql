CREATE TABLE IF NOT EXISTS public.process (
	id bigserial NOT NULL,
	dataset_id int8 NULL,
	dataflow_id int8 NULL,
	process_type varchar NULL,
	username varchar NULL,
	process_id varchar NULL,
	status varchar NULL,
	date_start timestamp(0) NULL,
	date_finish timestamp(0) NULL,
	queued_date timestamp(0) NULL,
	CONSTRAINT process_pk PRIMARY KEY (id)
);

GRANT ALL ON TABLE public.process TO testuser, dataflow, dataset, validation, recordstore;
GRANT ALL ON sequence public.process_id_seq TO testuser, dataflow, dataset, recordstore, validation;