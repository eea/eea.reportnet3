CREATE TABLE IF NOT EXISTS public.message_attachment (
	id bigserial NOT NULL,
	file_name text NULL,
	file_size text NULL,
	"content" bytea NULL,
	message_id int8 NULL,
	CONSTRAINT message_attachment_pk PRIMARY KEY (id),
	CONSTRAINT message_attachment_fk FOREIGN KEY (message_id) REFERENCES message(id)
);

GRANT ALL ON TABLE public.message_attachment TO testuser, dataflow, dataset, validation, recordstore;
