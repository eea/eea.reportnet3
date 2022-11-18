CREATE SEQUENCE IF NOT EXISTS public.internal_process_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
GRANT ALL ON SEQUENCE public.internal_process_id_seq TO testuser,dataflow,dataset,validation,recordstore;