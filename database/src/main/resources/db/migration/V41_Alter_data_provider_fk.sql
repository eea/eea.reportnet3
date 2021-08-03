INSERT INTO data_provider_group (id, type, name) SELECT id, type, label from data_provider;

update data_provider set group_id = id;

ALTER TABLE public.data_provider ADD CONSTRAINT data_provider_fk FOREIGN KEY (group_id) REFERENCES public.data_provider_group(id);

ALTER TABLE public.data_provider DROP COLUMN "type";