CREATE TABLE IF NOT EXISTS public.data_provider_group (
	id int8 NOT NULL,
	"name" varchar(255) NULL,
	"type" varchar(255) NULL,
	CONSTRAINT data_provider_gp_pk PRIMARY KEY (id)
);

GRANT ALL ON TABLE public.data_provider_group TO testuser, dataflow, dataset, validation, recordstore;
