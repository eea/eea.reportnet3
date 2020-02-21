
-- ALTERS

ALTER TABLE public.data_collection DROP CONSTRAINT dataset_data_collection_fkey;
ALTER TABLE public.data_collection ADD CONSTRAINT dataset_data_collection_fkey FOREIGN KEY (id) REFERENCES dataset(id) ON DELETE CASCADE;

ALTER TABLE public.partition_dataset DROP CONSTRAINT partition_dataset_dataset_fkey;
ALTER TABLE public.partition_dataset ADD CONSTRAINT partition_dataset_dataset_fkey FOREIGN KEY (id_dataset) REFERENCES dataset(id) ON DELETE CASCADE

ALTER TABLE public.reporting_dataset DROP CONSTRAINT reporting_dataset_dataset_fkey;
ALTER TABLE public.reporting_dataset ADD CONSTRAINT reporting_dataset_dataset_fkey FOREIGN KEY (id) REFERENCES dataset(id) ON DELETE CASCADE;