CREATE TABLE IF NOT EXISTS public.message (
	"id" bigserial NOT NULL,
	"dataflow_id" int8 NOT NULL,
	"provider_id" int8 NOT NULL,
	"content" varchar NOT NULL,
	"date" timestamp NOT NULL,
	"direction" boolean NOT NULL,
	"read" boolean NOT NULL,
	"user_name" varchar NOT NULL,
	CONSTRAINT message_pk PRIMARY KEY (id),
	CONSTRAINT message_dataflow_fk FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT message_provider_fk FOREIGN KEY (provider_id) REFERENCES data_provider(id) ON DELETE CASCADE ON UPDATE CASCADE
);

GRANT ALL ON TABLE public.message TO testuser, dataflow, dataset, validation, recordstore;