ALTER TABLE public.fme_jobs RENAME COLUMN idjob TO job_id;
ALTER TABLE public.fme_jobs ADD dataflow_id int8 NULL;
ALTER TABLE public.fme_jobs ADD provider_id int8 NULL;
ALTER TABLE public.fme_jobs ADD file_name varchar NULL;