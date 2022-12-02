--Drop column process_id from jobs table
ALTER TABLE public.jobs DROP COLUMN "process_id";
ALTER TABLE public.jobs ADD COLUMN IF NOT EXISTS "release" bool NOT NULL DEFAULT false;
