ALTER TABLE public.dataflow ADD COLUMN IF NOT EXISTS show_public_info bool NULL DEFAULT false;
ALTER TABLE public.dataset ADD COLUMN IF NOT EXISTS restrict_from_public bool NULL DEFAULT false;