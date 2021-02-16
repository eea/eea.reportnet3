ALTER TABLE public.dataflow ADD COLUMN IF NOT EXISTS available bool NULL DEFAULT false;
ALTER TABLE public.dataset ADD COLUMN IF NOT EXISTS restricted bool NULL DEFAULT false;