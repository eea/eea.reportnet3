
--- New boolean field to check if datasets has been created
ALTER TABLE public.representative ADD has_datasets bool NOT NULL DEFAULT true;

commit;