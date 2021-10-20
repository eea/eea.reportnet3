CREATE TABLE IF NOT EXISTS public.webform (
	id bigserial NOT NULL,
	"name" varchar(255) NULL,
	CONSTRAINT webforms_pk PRIMARY KEY (id)
);

GRANT ALL ON TABLE public.webform TO testuser, dataflow, dataset, validation, recordstore;