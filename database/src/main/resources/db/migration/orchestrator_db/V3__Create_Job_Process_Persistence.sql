--Create table and sequence for job_process--
CREATE TABLE IF NOT EXISTS public.job_process (
   id int8 NOT NULL,
   job_id int8 NOT NULL,
   process_id varchar NOT NULL,
   CONSTRAINT job_process_pk PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.job_process_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
