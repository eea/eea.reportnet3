ALTER TABLE public.dataflow ADD official_reporting bool NULL;

UPDATE public.dataflow
SET official_reporting = true
WHERE show_public_info = true;