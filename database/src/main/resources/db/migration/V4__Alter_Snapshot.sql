ALTER TABLE public."snapshot" ADD COLUMN IF NOT EXISTS "blocked" bool NULL;
ALTER TABLE public."snapshot" ADD COLUMN IF NOT EXISTS "date_released" timestamp NULL;

COMMIT;