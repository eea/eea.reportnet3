--Drop column process_id from jobs and job_history tables and add columns dataflow_id, data_provider_id, dataset_id
ALTER TABLE public.jobs DROP COLUMN "process_id";
ALTER TABLE public.jobs ADD COLUMN IF NOT EXISTS "release" bool NOT NULL DEFAULT false;
ALTER TABLE public.jobs ADD COLUMN IF NOT EXISTS "dataflow_id" int8 NULL;
ALTER TABLE public.jobs ADD COLUMN IF NOT EXISTS "provider_id" int8 NULL;
ALTER TABLE public.jobs ADD COLUMN IF NOT EXISTS "dataset_id" int8 NULL;

ALTER TABLE public.job_history DROP COLUMN "process_id";
ALTER TABLE public.job_history ADD COLUMN IF NOT EXISTS "release" bool NOT NULL DEFAULT false;
ALTER TABLE public.job_history ADD COLUMN IF NOT EXISTS "dataflow_id" int8 NULL;
ALTER TABLE public.job_history ADD COLUMN IF NOT EXISTS "provider_id" int8 NULL;
ALTER TABLE public.job_history ADD COLUMN IF NOT EXISTS "dataset_id" int8 NULL;
