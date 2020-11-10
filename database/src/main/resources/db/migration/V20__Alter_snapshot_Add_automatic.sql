ALTER TABLE public."snapshot" ADD COLUMN IF NOT EXISTS "automatic" bool NULL;

COMMIT;