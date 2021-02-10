ALTER TABLE public.dataflow ADD COLUMN IF NOT EXISTS ispublic bool NULL DEFAULT false;
ALTER TABLE public.dataset ADD COLUMN IF NOT EXISTS isrestricted bool NULL DEFAULT false;