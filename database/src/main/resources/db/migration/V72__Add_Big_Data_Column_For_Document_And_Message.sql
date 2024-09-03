ALTER TABLE public.document ADD COLUMN IF NOT EXISTS "big_data" bool NOT NULL DEFAULT false;

ALTER TABLE public.message ADD COLUMN IF NOT EXISTS "big_data" bool NOT NULL DEFAULT false;