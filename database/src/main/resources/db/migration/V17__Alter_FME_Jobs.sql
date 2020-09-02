DROP TABLE IF EXISTS public.fme_jobs;
CREATE TABLE IF NOT EXISTS public.fme_jobs (
	id bigserial NOT NULL,
	job_id int8 NULL,
	dataset_id int8 NOT NULL,
	dataflow_id int8 NOT NULL,
	provider_id int8 NULL,
	file_name varchar NULL,
	user_name varchar NOT NULL,
	operation varchar NOT NULL,	
	status varchar NOT NULL,
	CONSTRAINT fme_jobs_pk PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.fme_jobs_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

GRANT ALL ON TABLE public.fme_jobs TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON SEQUENCE public.fme_jobs_id_seq TO testuser,dataflow,dataset,validation,recordstore;