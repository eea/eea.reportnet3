ALTER TABLE public."snapshot" ADD "blocked" bool NULL;
ALTER TABLE public."snapshot" ADD "date_released" timestamp NULL;

COMMIT;