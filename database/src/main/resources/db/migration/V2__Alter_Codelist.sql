
--- Originally the column id_category was set to NOT NULL. That property has to be deleted, the column can be null
ALTER TABLE public.codelist ALTER COLUMN id_category SET NULL;

COMMIT;