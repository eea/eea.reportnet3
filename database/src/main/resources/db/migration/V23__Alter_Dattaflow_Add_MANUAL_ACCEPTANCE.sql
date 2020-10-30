ALTER TABLE public."dataflow" ADD COLUMN IF NOT EXISTS "manual_acceptance" bool NULL;

COMMIT;