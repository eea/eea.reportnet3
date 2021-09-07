
ALTER TABLE public.message ADD COLUMN IF NOT EXISTS automatic bool NOT NULL DEFAULT false;