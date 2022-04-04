ALTER TABLE public.process ADD column if not exists priority int4 default 50;

CREATE TABLE if not exists public.task (
	process_id varchar NOT NULL,
	"version" int4 NULL,
	"json" varchar NULL,
	pod varchar NULL,
	date_start timestamp NULL,
	date_finish timestamp NULL,
	create_date timestamp NULL,
	status varchar NULL,
	id bigserial NOT NULL,
	CONSTRAINT task_pk PRIMARY KEY (id)
);
CREATE INDEX if not exists task_process_id_idx ON public.task USING btree (process_id);


ALTER TABLE public.task OWNER TO testuser;
GRANT ALL ON TABLE public.task TO testuser, dataflow, dataset, recordstore, validation;

GRANT ALL ON sequence public.task_id_seq TO testuser, dataflow, dataset, recordstore, validation;