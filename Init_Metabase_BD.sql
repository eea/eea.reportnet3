DROP SCHEMA public cascade;
CREATE SCHEMA public;
commit;

--TABLES

CREATE TABLE public.dataflow (
	id bigserial NOT NULL,
	description varchar(255) NULL,
	"name" varchar(255) NULL,
	creation_date timestamp null,
	DEADLINE_DATE timestamp null,
	status VARCHAR null,
	CONSTRAINT dataflow_pkey PRIMARY KEY (id)
);

CREATE TABLE public.dataset (
	id bigserial NOT NULL,
	date_creation timestamp NULL,
	DATASET_NAME varchar(255) NULL,
	dataflowid int8 NULL,
	status varchar(255) NULL,
	url_connection varchar(255) NULL,
	visibility varchar(255) NULL,
	dataset_schema varchar(255) NULL,
	CONSTRAINT dataset_pkey PRIMARY KEY (id)
);

CREATE TABLE public.contributor (
	id bigserial NOT NULL,
	email varchar(255) NULL,
	user_id varchar(255) NULL,
	dataflow_id serial NOT NULL,
	CONSTRAINT contributor_pkey PRIMARY KEY (id),
	CONSTRAINT dataflow_contributor_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id)
);

CREATE TABLE public.data_collection (
	duedate timestamp NULL,
	"name" varchar(255) NULL,
	visible bool NULL,
	id bigserial NOT NULL,
	CONSTRAINT data_collection_pkey PRIMARY KEY (id),
	CONSTRAINT dataset_data_collection_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public.design_dataset (
	"type" varchar(255) NULL,
	id serial NOT NULL,
	CONSTRAINT design_dataset_pkey PRIMARY KEY (id),
	CONSTRAINT dataset_design_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public."document" (
	id bigserial NOT NULL,
	"language" varchar(255) NULL,
	"name" varchar(255) NULL,
	description varchar(255) NULL,
	dataflow_id serial NOT NULL,
	CONSTRAINT document_pkey PRIMARY KEY (id),
	CONSTRAINT document_dataflow_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id)
);

CREATE TABLE public.eu_dataset (
	"name" varchar(255) NULL,
	visible bool NULL,
	id bigserial NOT NULL,
	CONSTRAINT eu_dataset_pkey PRIMARY KEY (id),
	CONSTRAINT eu_dataset_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public.partition_dataset (
	id bigserial NOT NULL,
	user_name varchar(255) NULL,
	id_dataset serial NOT NULL,
	CONSTRAINT partition_dataset_pkey PRIMARY KEY (id),
	CONSTRAINT partition_dataset_dataset_fkey FOREIGN KEY (id_dataset) REFERENCES dataset(id)
);

CREATE TABLE public.reporting_dataset (
	id bigserial NOT NULL,
	CONSTRAINT reporting_dataset_pkey PRIMARY KEY (id),
	CONSTRAINT reporting_dataset_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public."snapshot" (
	datacollection_id int8 NULL,
	"description" varchar(255) NULL,
	REPORTING_DATASET_ID int8 null,
	id bigserial NOT NULL,
	"release" bool NULL DEFAULT false,
	CONSTRAINT snapshot_pkey PRIMARY KEY (id),
	CONSTRAINT snapshot_data_collection_fkey FOREIGN KEY (datacollection_id) REFERENCES data_collection(id),
	CONSTRAINT snapshot_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public.submission_agreement (
	id bigserial NOT NULL,
	description varchar(255) NULL,
	"name" varchar(255) NULL,
	dataflow_id serial NOT NULL,
	CONSTRAINT submission_agreement_pkey PRIMARY KEY (id),
	CONSTRAINT submission_agreement_dataflow_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id)
);

CREATE TABLE public.table_collection (
	id bigserial NOT NULL,
	dataflow_id int8 NULL,
	dataset_id int8 NULL,
	table_name varchar(255) NULL,
	CONSTRAINT table_collection_pkey PRIMARY KEY (id)
);

CREATE TABLE public.table_headers_collection (
	id bigserial NOT NULL,
	header_name varchar(255) NULL,
	header_type varchar(255) NULL,
	id_table serial NOT NULL,
	CONSTRAINT table_headers_collection_pkey PRIMARY KEY (id),
	CONSTRAINT table_headers_collection_table_collection_fkey FOREIGN KEY (id_table) REFERENCES table_collection(id)
);

CREATE TABLE public.weblink (
	id bigserial NOT NULL,
	description varchar(255) NULL,
	url varchar(255) NULL,
	dataflow_id serial NOT NULL,
	CONSTRAINT weblink_pkey PRIMARY KEY (id),
	CONSTRAINT weblink_dataflow_fkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id)
);

CREATE TABLE public.USER_REQUEST (
	id bigserial NOT NULL,
	USER_REQUESTER varchar(255) NULL,
	USER_REQUESTED varchar(255) NULL,
	REQUEST_TYPE VARCHAR NULL,
	CONSTRAINT USER_REQUEST_PKEY PRIMARY KEY (id)
	
);

CREATE TABLE public.dataflow_user_request (
	dataflow_id bigserial NOT NULL,
	user_request_id bigserial NOT NULL,
	CONSTRAINT dataflow_user_request_pkey PRIMARY KEY (dataflow_id, user_request_id),
	CONSTRAINT user_request_pkey FOREIGN KEY (user_request_id) REFERENCES user_request(id),
	CONSTRAINT user_request_DATAFLOW_pkey FOREIGN KEY (dataflow_id) REFERENCES dataflow(id)
);

CREATE TABLE public.lock (
	id int4 NOT NULL,
	create_date timestamp NULL,
	created_by varchar NULL,
	lock_type int4 NULL,
	lock_criteria bytea NULL,
	CONSTRAINT lock_pkey PRIMARY KEY (id)
);

CREATE TABLE public."snapshot_schema" (
	id bigserial NOT null,
	"description" varchar(255) NULL,
	DESIGN_DATASET_ID int8 null,
	CONSTRAINT snapshot_schema_pkey PRIMARY KEY (id),
	CONSTRAINT snapshot_schema_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id)
);

CREATE TABLE public."statistics" (
	id bigserial NOT NULL,
	id_dataset bigserial NULL,
	id_table_schema text NULL,
	stat_name text NULL,
	value text NULL,
	CONSTRAINT statistics_pkey PRIMARY KEY (id)
);




--GRANTS

GRANT USAGE ON SCHEMA public TO testuser ;
GRANT USAGE ON SCHEMA public TO testuser ;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO testuser ;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO testuser ;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO testuser ;

ALTER TABLE public.contributor OWNER TO testuser;
GRANT ALL ON TABLE public.contributor TO testuser;
ALTER TABLE public.data_collection OWNER TO testuser;
GRANT ALL ON TABLE public.data_collection TO testuser;
ALTER TABLE public.dataflow OWNER TO testuser;
GRANT ALL ON TABLE public.dataflow TO testuser;
ALTER TABLE public.dataset OWNER TO testuser;
GRANT ALL ON TABLE public.dataset TO testuser;
ALTER TABLE public.design_dataset OWNER TO testuser;
GRANT ALL ON TABLE public.design_dataset TO testuser;
ALTER TABLE public."document" OWNER TO testuser;
GRANT ALL ON TABLE public."document" TO testuser;
ALTER TABLE public.eu_dataset OWNER TO testuser;
GRANT ALL ON TABLE public.eu_dataset TO testuser;
ALTER TABLE public.partition_dataset OWNER TO testuser;
GRANT ALL ON TABLE public.partition_dataset TO testuser;
ALTER TABLE public.reporting_dataset OWNER TO testuser;
GRANT ALL ON TABLE public.reporting_dataset TO testuser;
ALTER TABLE public."snapshot" OWNER TO testuser;
GRANT ALL ON TABLE public."snapshot" TO testuser;
ALTER TABLE public.submission_agreement OWNER TO testuser;
GRANT ALL ON TABLE public.submission_agreement TO testuser;
ALTER TABLE public.table_collection OWNER TO testuser;
GRANT ALL ON TABLE public.table_collection TO testuser;
ALTER TABLE public.table_headers_collection OWNER TO testuser;
GRANT ALL ON TABLE public.table_headers_collection TO testuser;
ALTER TABLE public.weblink OWNER TO testuser;
GRANT ALL ON TABLE public.weblink TO testuser;
ALTER TABLE public.USER_REQUEST OWNER TO testuser;
GRANT ALL ON TABLE public.USER_REQUEST TO testuser;
ALTER TABLE public.dataflow_user_request OWNER TO testuser;
GRANT ALL ON TABLE public.dataflow_user_request TO testuser;
ALTER TABLE public.lock OWNER TO testuser;
GRANT ALL ON TABLE public.lock TO testuser;
ALTER TABLE public.snapshot_schema OWNER TO testuser;
GRANT ALL ON TABLE public.snapshot_schema TO testuser;
ALTER TABLE public.statistics OWNER TO testuser;
GRANT ALL ON TABLE public.statistics TO testuser;

--INDEXES--
CREATE INDEX INDX_ISRELEASED ON SNAPSHOT (release);
CREATE INDEX INDX_REPORTING_DS_ID ON SNAPSHOT (reporting_dataset_id);

CREATE INDEX statistics_id_dataset_idx ON public.statistics (id_dataset);



COMMIT;