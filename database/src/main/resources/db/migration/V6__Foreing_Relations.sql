
--NEW TABLE FOREIGN RELATIONS TO CONTROL THE RELATIONS BETWEEN DATASETS ON REFERENCED FIELDS OF THE SCHEMA

CREATE TABLE IF NOT EXISTS public.FOREIGN_RELATIONS (
	ID bigserial not null, 
	ID_PK varchar(255), 
	DATASET_ID_DESTINATION bigint, 
	DATASET_ID_ORIGIN bigint, 
	id_fk_origin varchar NULL,
	CONSTRAINT foreign_relations_pkey PRIMARY KEY (id),
	CONSTRAINT foreign_relations_destination_fkey FOREIGN KEY (dataset_id_destination) REFERENCES dataset(id) ON DELETE CASCADE,
	CONSTRAINT foreign_relations_origin_fkey FOREIGN KEY (dataset_id_origin) REFERENCES dataset(id) ON DELETE CASCADE
);


ALTER TABLE public.foreign_relations OWNER TO testuser;
GRANT ALL ON TABLE public.foreign_relations TO testuser;

