CREATE TABLE public.test_dataset (
	id bigserial NOT NULL,
	CONSTRAINT test_dataset_pkey PRIMARY KEY (id),
	CONSTRAINT test_dataset_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

GRANT ALL ON TABLE public.test_dataset TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON SEQUENCE test_dataset_id_seq TO testuser,dataflow,dataset,validation,recordstore;