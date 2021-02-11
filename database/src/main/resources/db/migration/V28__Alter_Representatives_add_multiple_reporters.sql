ALTER TABLE public.representative ADD CONSTRAINT representative_primarykey PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS public."user" (
	id varchar NOT NULL,
	usermail varchar NULL,
	CONSTRAINT user_pk PRIMARY KEY (id)
);

ALTER TABLE public."user" OWNER TO root;
GRANT ALL ON TABLE public."user" TO root;

CREATE TABLE IF NOT EXISTS public.representative_user (
	representative_id int8 NOT NULL,
	user_id varchar NOT NULL,
	CONSTRAINT representative_user_fk FOREIGN KEY (representative_id) REFERENCES representative(id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT representative_user_fk_1 FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE
);


ALTER TABLE public.representative_user OWNER TO root;
GRANT ALL ON TABLE public.representative_user TO root;