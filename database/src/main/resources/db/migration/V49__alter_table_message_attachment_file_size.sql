ALTER TABLE public.message ADD COLUMN IF NOT EXISTS "file_size" text not null default(0);

ALTER TABLE public.message_attachment DROP COLUMN IF EXISTS "file_size";