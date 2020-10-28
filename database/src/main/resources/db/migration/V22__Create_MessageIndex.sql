CREATE INDEX IF NOT EXISTS message_date_idx ON public.message ("date");
GRANT ALL ON SEQUENCE public.message_id_seq TO testuser, recordstore, dataset, dataflow, validation;
COMMIT;