
create sequence if not exists representative_id_seq INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	CACHE 1
	NO CYCLE;

GRANT ALL ON sequence public.representative_id_seq TO testuser;

commit;
