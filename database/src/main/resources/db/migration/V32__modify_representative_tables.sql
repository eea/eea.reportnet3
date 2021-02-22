CREATE TABLE if not exists public.representative_leadreporter (
	representative_id int8 NOT NULL,
	email varchar NOT NULL,
	id int8 NOT NULL,
	CONSTRAINT representative_leadreporter_pk PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.representative_leadreporter OWNER TO testuser, dataflow, dataset, recordstore, validation;
GRANT ALL ON TABLE public.representative_leadreporter TO testuser, dataflow, dataset, recordstore, validation;


create sequence if not exists leadreporter_id_seq INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	CACHE 1
	NO CYCLE;

GRANT ALL ON sequence public.leadreporter_id_seq TO testuser, dataflow, dataset, recordstore, validation;


drop table if exists public."user";

drop table if exists public."representative_user";


insert into public.representative_leadreporter (id, representative_id, email) (select nextval('leadreporter_id_seq') as id, r.id as representative_id, r.user_mail as email from public.representative r  where r.user_mail is not null) on conflict do nothing;

