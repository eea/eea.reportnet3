CREATE TABLE if not exists public.changes_eudataset (
	id bigserial NOT NULL,
	datacollection int8,
	provider varchar NULL,
	
	CONSTRAINT changes_pk PRIMARY KEY (id)
);

ALTER TABLE public.changes_eudataset OWNER TO testuser;
GRANT ALL ON TABLE public.changes_eudataset TO testuser, dataflow, dataset, validation, recordstore;
GRANT ALL ON sequence public.changes_eudataset_id_seq TO testuser, dataflow, dataset, recordstore, validation;