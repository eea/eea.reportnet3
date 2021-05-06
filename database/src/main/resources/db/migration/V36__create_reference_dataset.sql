CREATE TABLE IF NOT EXISTS public.reference_dataset (
	id bigserial NOT NULL,
	CONSTRAINT referencedataset_pkey PRIMARY KEY (id),
	CONSTRAINT referencedataset_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

GRANT ALL ON TABLE public.reference_dataset TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON SEQUENCE reference_dataset_id_seq TO testuser,dataflow,dataset,validation,recordstore;