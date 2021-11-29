CREATE TABLE IF NOT EXISTS public.temp_user (
	id serial NOT NULL,
	email varchar NOT NULL,
	usertype varchar NOT NULL,
	dataflowid int8 NOT NULL,
	registered timestamp(0) NULL,
	CONSTRAINT temp_user_pk PRIMARY KEY (id)
);
