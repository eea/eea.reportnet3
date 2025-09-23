ALTER TABLE public.dataset
ADD COLUMN IF NOT EXISTS date_status_changed TIMESTAMP NULL;
