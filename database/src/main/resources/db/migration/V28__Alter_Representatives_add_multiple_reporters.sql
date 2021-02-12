ALTER TABLE public.representative ADD CONSTRAINT representative_primarykey PRIMARY KEY (id);

CREATE TABLE public."user" (
	user_mail varchar NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (user_mail)
);

ALTER TABLE public."user" OWNER TO root;
GRANT ALL ON TABLE public."user" TO root;

CREATE TABLE public.representative_user (
	representative_id int8 NOT NULL,
	user_mail varchar NOT NULL,
	CONSTRAINT representative_user_pk PRIMARY KEY (representative_id, user_mail),
	CONSTRAINT representative_user_fk FOREIGN KEY (representative_id) REFERENCES representative(id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT representative_user_fk_1 FOREIGN KEY (user_mail) REFERENCES "user"(user_mail)
);

ALTER TABLE public.representative_user OWNER TO root;
GRANT ALL ON TABLE public.representative_user TO root;
