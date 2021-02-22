ALTER TABLE public.representative ADD COLUMN IF NOT EXISTS restrict_from_public bool NULL DEFAULT false;
ALTER TABLE public.dataset RENAME COLUMN restrict_from_public TO available_in_public;