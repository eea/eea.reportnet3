
-- DROP AND ADD AGAIN THE DIFFERENT FOREIGN KEYS OF DATASET METABASE. TO ADD DELETE CASCADE WHEN A DESIGN DATASET IS DELETED

ALTER TABLE public.partition_dataset DROP CONSTRAINT IF EXISTS partition_dataset_dataset_fkey;
ALTER TABLE public.partition_dataset ADD CONSTRAINT partition_dataset_dataset_fkey FOREIGN KEY (id_dataset) REFERENCES dataset(id) ON DELETE CASCADE;

ALTER TABLE public.design_dataset DROP CONSTRAINT IF EXISTS dataset_design_fkey;
ALTER TABLE public.design_dataset ADD CONSTRAINT dataset_design_fkey FOREIGN KEY (id) REFERENCES dataset(id) ON DELETE CASCADE;



commit;
