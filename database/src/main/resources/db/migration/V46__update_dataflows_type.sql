--SET TYPE TO REPORTING WHERE DATAFLOW TYPES ARE NULL. NOW THE REGULAR REPORTINGS ARE "REPORTING"
UPDATE public.dataflow SET type='REPORTING' WHERE type is null;