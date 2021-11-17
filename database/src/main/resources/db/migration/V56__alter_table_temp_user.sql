ALTER TABLE public.temp_user ADD COLUMN IF NOT EXISTS data_provider_id int8 NULL;
ALTER TABLE public.temp_user RENAME COLUMN usertype TO user_type;
ALTER TABLE public.temp_user RENAME COLUMN dataflowid TO dataflow_id;

