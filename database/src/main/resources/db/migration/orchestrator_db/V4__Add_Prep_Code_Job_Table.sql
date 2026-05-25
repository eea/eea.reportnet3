-- add preparation code to jobs
ALTER TABLE public.jobs
    ADD COLUMN preparation_code VARCHAR(255);