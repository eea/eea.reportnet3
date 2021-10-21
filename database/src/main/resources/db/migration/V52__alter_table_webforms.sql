ALTER TABLE public.webform RENAME COLUMN "name" TO "label";
ALTER TABLE public.webform ADD value varchar(255) NULL;
