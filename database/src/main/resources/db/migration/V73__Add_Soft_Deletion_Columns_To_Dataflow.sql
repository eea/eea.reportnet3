ALTER TABLE public.dataflow
ADD COLUMN "is_deleted" boolean NOT NULL DEFAULT false,
ADD COLUMN "deleted_at" timestamp NULL;
