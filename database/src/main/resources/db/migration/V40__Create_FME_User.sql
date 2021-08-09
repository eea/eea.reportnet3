CREATE TABLE IF NOT EXISTS public.fme_user (
	id bigserial NOT NULL,
	user_name varchar NOT NULL,
	password varchar NOT NULL,	
	CONSTRAINT fme_user_pk PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.fme_user_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

GRANT ALL ON TABLE public.fme_user TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON SEQUENCE public.fme_user_id_seq TO testuser,dataflow,dataset,validation,recordstore;

ALTER TABLE public.dataflow ADD column if not exists "dataprovider_group_id" int8 NULL;
ALTER TABLE public.dataflow ADD column if not exists "fme_user_id" int8 NULL;
ALTER TABLE public.dataflow ADD CONSTRAINT dataflow_fme_user_fkey FOREIGN KEY (fme_user_id) REFERENCES fme_user(id);

