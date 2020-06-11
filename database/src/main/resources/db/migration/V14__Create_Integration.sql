-- NEW INTEGRATION TABLE AND ITS OPERATION PARAMETERS

CREATE TABLE IF NOT EXISTS public.integration (
	id bigserial NOT NULL,
	dataflow_id int8 NULL,
	"name" varchar(255) NULL,
	"description" varchar(255) NULL,
	"tool" varchar(255) NULL,
	"operation" varchar(255) NULL,	
	CONSTRAINT integration_pkey PRIMARY KEY (id),
	CONSTRAINT dataflow_fk FOREIGN KEY (dataflow_id) REFERENCES dataflow(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS public.integration_operation_parameters (
	id bigserial NOT NULL,
	"parameter_type" varchar(255) null,
	integration_id int8 NULL,
	"parameter" varchar(255) null,
	"value" varchar(255) null,
	CONSTRAINT integration_operation_parameter_pkey PRIMARY KEY (id),
	CONSTRAINT integration_operation_parameter_fk FOREIGN KEY (integration_id) REFERENCES integration(id) ON DELETE CASCADE
);

ALTER TABLE public.integration OWNER TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON TABLE public.integration TO testuser,dataflow,dataset,validation,recordstore;

ALTER TABLE public.integration_operation_parameters OWNER TO testuser,dataflow,dataset,validation,recordstore;
GRANT ALL ON TABLE public.integration_operation_parameters TO testuser,dataflow,dataset,validation,recordstore;