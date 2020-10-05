ALTER TABLE public."snapshot" ADD COLUMN IF NOT EXISTS "eu_released" bool NULL;
ALTER TABLE public."snapshot" RENAME COLUMN "release" TO "dc_released";

COMMIT;