--Create table and sequence for jobs--
CREATE TABLE IF NOT EXISTS public.jobs (
   id int8 NOT NULL,
   job_type varchar NOT NULL,
   job_status varchar NOT NULL,
   date_added timestamp NOT NULL,
   date_status_changed timestamp NOT NULL,
   parameters varchar NULL,
   creator_username varchar NULL,
   CONSTRAINT jobs_pk PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.jobs_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

--Create table and sequence for job history--
CREATE TABLE IF NOT EXISTS public.job_history (
    id int8 NOT NULL,
    job_id int8 NOT NULL,
    job_type varchar NOT NULL,
    job_status varchar NOT NULL,
    date_added timestamp NOT NULL,
    date_status_changed timestamp NOT NULL,
    parameters varchar NULL,
    creator_username varchar NULL,
    CONSTRAINT job_history_pk PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.job_history_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;