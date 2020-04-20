ALTER TABLE public.data_provider DROP CONSTRAINT IF EXISTS unique_data_provider;
ALTER TABLE public.data_provider ADD CONSTRAINT unique_data_provider UNIQUE ("type",code);