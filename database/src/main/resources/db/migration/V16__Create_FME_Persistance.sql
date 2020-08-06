CREATE TABLE IF NOT EXISTS public.fme_jobs (
	idjob int8 NULL,
	dataset_id int8 NULL,
	"r3user" varchar NULL,
	"operation" varchar NULL,
	status varchar NULL
);

GRANT ALL ON TABLE public.fme_jobs TO testuser,dataflow,dataset,validation,recordstore;