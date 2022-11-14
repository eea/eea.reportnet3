CREATE TABLE IF NOT EXISTS public.internal_process (
id int8 NOT NULL,
type varchar NOT NULL,
dataflow_id int8 NOT NULL,
data_provider_id int8 NOT NULL,
data_collection_id int8 NOT NULL,
transaction_id varchar NOT NULL,
aggregate_id varchar NOT NULL,
CONSTRAINT internal_process_pk PRIMARY KEY (id)
);

GRANT ALL ON TABLE public.internal_process TO testuser,dataflow,dataset,validation,recordstore;