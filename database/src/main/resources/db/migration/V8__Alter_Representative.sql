
--- New boolean field to check if datasets has been created
ALTER TABLE public.representative ADD COLUMN IF NOT EXISTS has_datasets bool NOT NULL DEFAULT true;

commit;